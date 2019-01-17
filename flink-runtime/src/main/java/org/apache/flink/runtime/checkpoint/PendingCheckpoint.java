/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.checkpoint;

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.checkpoint.savepoint.Savepoint;
import org.apache.flink.runtime.checkpoint.savepoint.SavepointV2;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.executiongraph.ExecutionVertex;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.jobgraph.OperatorID;
import org.apache.flink.runtime.state.CheckpointMetadataOutputStream;
import org.apache.flink.runtime.state.CheckpointStorageLocation;
import org.apache.flink.runtime.state.CompletedCheckpointStorageLocation;
import org.apache.flink.runtime.state.StateUtil;
import org.apache.flink.util.ExceptionUtils;
import org.apache.flink.util.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;

import static org.apache.flink.util.Preconditions.checkArgument;
import static org.apache.flink.util.Preconditions.checkNotNull;
import static org.apache.flink.util.Preconditions.checkState;

/**
 * A pending checkpoint is a checkpoint that has been started, but has not been
 * acknowledged by all tasks that need to acknowledge it. Once all tasks have
 * acknowledged it, it becomes a {@link CompletedCheckpoint}.
 *
 * <p>Note that the pending checkpoint, as well as the successful checkpoint keep the
 * state handles always as serialized values, never as actual values.
 */
public class PendingCheckpoint {

	/**
	 * Result of the {@link PendingCheckpoint#acknowledgedTasks} method.
	 */
	public enum TaskAcknowledgeResult {
		SUCCESS, // successful acknowledge of the task
		DUPLICATE, // acknowledge message is a duplicate
		UNKNOWN, // unknown task acknowledged
		DISCARDED // pending checkpoint has been discarded
	}

	// ------------------------------------------------------------------------

	/** The PendingCheckpoint logs to the same logger as the CheckpointCoordinator. */
	private static final Logger LOG = LoggerFactory.getLogger(CheckpointCoordinator.class);

	private final Object lock = new Object();

	private final JobID jobId;

	private final long checkpointId;

	private final long checkpointTimestamp;

	private volatile CheckpointStatus checkpointStatus;

	private volatile long terminateTimestamp;

	private volatile String failure;

	private final Map<ExecutionAttemptID, ExecutionVertex> notYetAcknowledgedTasks;

	private final Set<ExecutionAttemptID> acknowledgedTasks;

	private final List<MasterState> masterState;

	private final Map<OperatorID, OperatorState> operatorStates;

	private final Map<JobVertexID, VertexCheckpointTracker> vertexTrackers;

	/** The checkpoint properties. */
	private final CheckpointProperties props;

	/** Target storage location to persist the checkpoint metadata to. */
	private final CheckpointStorageLocation targetLocation;

	/** The promise to fulfill once the checkpoint has been completed. */
	private final CompletableFuture<CompletedCheckpoint> onCompletionPromise;

	/** The executor for potentially blocking I/O operations, like state disposal. */
	private final Executor executor;

	private int numAcknowledgedTasks;

	private boolean discarded;

	private volatile ScheduledFuture<?> cancellerHandle;

	// --------------------------------------------------------------------------------------------

	public PendingCheckpoint(
			JobID jobId,
			long checkpointId,
			long checkpointTimestamp,
			Map<ExecutionAttemptID, ExecutionVertex> verticesToConfirm,
			CheckpointProperties props,
			CheckpointStorageLocation targetLocation,
			Executor executor) {

		checkArgument(verticesToConfirm.size() > 0,
				"Checkpoint needs at least one vertex that commits the checkpoint");

		this.jobId = checkNotNull(jobId);
		this.checkpointId = checkpointId;
		this.checkpointTimestamp = checkpointTimestamp;
		this.notYetAcknowledgedTasks = checkNotNull(verticesToConfirm);
		this.props = checkNotNull(props);
		this.targetLocation = checkNotNull(targetLocation);
		this.executor = Preconditions.checkNotNull(executor);

		this.checkpointStatus = CheckpointStatus.PENDING;
		this.terminateTimestamp = -1;
		this.failure = null;
		this.operatorStates = new HashMap<>();
		this.masterState = new ArrayList<>();
		this.acknowledgedTasks = new HashSet<>(verticesToConfirm.size());
		this.onCompletionPromise = new CompletableFuture<>();

		this.vertexTrackers = new HashMap<>();

		Map<JobVertexID, Set<Integer>> tasksToConfirmByVertex = new HashMap<>();
		for (ExecutionVertex task : verticesToConfirm.values()) {
			JobVertexID vertexId = task.getJobvertexId();
			int taskIndex = task.getParallelSubtaskIndex();

			Set<Integer> vertexTasksToConfirm =
				tasksToConfirmByVertex.computeIfAbsent(vertexId, (k) -> new HashSet<>());
			vertexTasksToConfirm.add(taskIndex);
		}

		for (Map.Entry<JobVertexID, Set<Integer>> vertexEntry : tasksToConfirmByVertex.entrySet()) {
			JobVertexID vertexId = vertexEntry.getKey();
			Set<Integer> vertexTasksToConform = vertexEntry.getValue();
			VertexCheckpointTracker vertexTracker = new VertexCheckpointTracker(vertexTasksToConform);
			vertexTrackers.put(vertexId, vertexTracker);
		}
	}

	// --------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------
	//  Properties
	// ------------------------------------------------------------------------

	public JobID getJobId() {
		return jobId;
	}

	public long getCheckpointId() {
		return checkpointId;
	}

	public long getCheckpointTimestamp() {
		return checkpointTimestamp;
	}

	public int getNumberOfNonAcknowledgedTasks() {
		return notYetAcknowledgedTasks.size();
	}

	public int getNumberOfAcknowledgedTasks() {
		return numAcknowledgedTasks;
	}

	public Map<OperatorID, OperatorState> getOperatorStates() {
		return operatorStates;
	}

	public Map<JobVertexID, VertexCheckpointTracker> getVertexTrackers() {
		return vertexTrackers;
	}

	public CheckpointStatus getCheckpointStatus() {
		return checkpointStatus;
	}

	public long getTerminateTimestamp() {
		return terminateTimestamp;
	}

	public String getFailure() {
		return failure;
	}

	public boolean isFullyAcknowledged() {
		return this.notYetAcknowledgedTasks.isEmpty() && !discarded;
	}

	public boolean isAcknowledgedBy(ExecutionAttemptID executionAttemptId) {
		return !notYetAcknowledgedTasks.containsKey(executionAttemptId);
	}

	public boolean isDiscarded() {
		return discarded;
	}

	/**
	 * Checks whether this checkpoint can be subsumed or whether it should always continue, regardless
	 * of newer checkpoints in progress.
	 *
	 * @return True if the checkpoint can be subsumed, false otherwise.
	 */
	public boolean canBeSubsumed() {
		// If the checkpoint is forced, it cannot be subsumed.
		return !props.forceCheckpoint();
	}

	CheckpointProperties getProps() {
		return props;
	}

	/**
	 * Sets the handle for the canceller to this pending checkpoint. This method fails
	 * with an exception if a handle has already been set.
	 *
	 * @return true, if the handle was set, false, if the checkpoint is already disposed;
	 */
	public boolean setCancellerHandle(ScheduledFuture<?> cancellerHandle) {
		synchronized (lock) {
			if (this.cancellerHandle == null) {
				if (!discarded) {
					this.cancellerHandle = cancellerHandle;
					return true;
				} else {
					return false;
				}
			}
			else {
				throw new IllegalStateException("A canceller handle was already set");
			}
		}
	}

	// ------------------------------------------------------------------------
	//  Progress and Completion
	// ------------------------------------------------------------------------

	/**
	 * Returns the completion future.
	 *
	 * @return A future to the completed checkpoint
	 */
	public CompletableFuture<CompletedCheckpoint> getCompletionFuture() {
		return onCompletionPromise;
	}

	public void complete() {
		synchronized (lock) {
			checkState(isFullyAcknowledged(), "Pending checkpoint has not been fully acknowledged yet.");

			terminateTimestamp = System.currentTimeMillis();
			checkpointStatus = CheckpointStatus.COMPLETED;
		}
	}

	public CompletedCheckpoint finalizeCheckpoint() throws IOException {

		synchronized (lock) {
			checkState(isFullyAcknowledged(), "Pending checkpoint has not been fully acknowledged yet.");

			// make sure we fulfill the promise with an exception if something fails
			try {
				// write out the metadata
				final Savepoint savepoint = new SavepointV2(checkpointId, operatorStates.values(), masterState);
				final CompletedCheckpointStorageLocation finalizedLocation;

				try (CheckpointMetadataOutputStream out = targetLocation.createMetadataOutputStream()) {
					Checkpoints.storeCheckpointMetadata(savepoint, out);
					finalizedLocation = out.closeAndFinalizeCheckpoint();
				}

				CompletedCheckpoint completed = new CompletedCheckpoint(
						jobId,
						checkpointId,
						checkpointTimestamp,
						System.currentTimeMillis(),
						operatorStates,
						masterState,
						props,
						finalizedLocation);

				onCompletionPromise.complete(completed);

				// mark this pending checkpoint as disposed, but do NOT drop the state
				dispose(false);

				return completed;
			}
			catch (Throwable t) {
				onCompletionPromise.completeExceptionally(t);
				ExceptionUtils.rethrowIOException(t);
				return null; // silence the compiler
			}
		}
	}

	/**
	 * Acknowledges the task with the given execution attempt id and the given subtask state.
	 *
	 * @param executionAttemptId of the acknowledged task
	 * @param operatorSubtaskStates of the acknowledged task
	 * @param taskCheckpointTrace Checkpoint metrics for the stats
	 * @return TaskAcknowledgeResult of the operation
	 */
	public TaskAcknowledgeResult acknowledgeTask(
		ExecutionAttemptID executionAttemptId,
		TaskStateSnapshot operatorSubtaskStates,
		TaskCheckpointTrace taskCheckpointTrace
	) {

		synchronized (lock) {
			if (discarded) {
				return TaskAcknowledgeResult.DISCARDED;
			}

			final ExecutionVertex vertex = notYetAcknowledgedTasks.remove(executionAttemptId);

			if (vertex == null) {
				if (acknowledgedTasks.contains(executionAttemptId)) {
					return TaskAcknowledgeResult.DUPLICATE;
				} else {
					return TaskAcknowledgeResult.UNKNOWN;
				}
			} else {
				acknowledgedTasks.add(executionAttemptId);
			}

			List<OperatorID> operatorIDs = vertex.getJobVertex().getOperatorIDs();
			int subtaskIndex = vertex.getParallelSubtaskIndex();

			if (operatorSubtaskStates != null) {
				for (OperatorID operatorID : operatorIDs) {

					OperatorSubtaskState operatorSubtaskState =
						operatorSubtaskStates.getSubtaskStateByOperatorID(operatorID);

					// if no real operatorSubtaskState was reported, we insert an empty state
					if (operatorSubtaskState == null) {
						operatorSubtaskState = new OperatorSubtaskState();
					}

					OperatorState operatorState = operatorStates.get(operatorID);

					if (operatorState == null) {
						operatorState = new OperatorState(
							operatorID,
							vertex.getTotalNumberOfParallelSubtasks(),
							vertex.getMaxParallelism());
						operatorStates.put(operatorID, operatorState);
					}

					operatorState.putState(subtaskIndex, operatorSubtaskState);
				}
			}

			++numAcknowledgedTasks;

			JobVertexID vertexId = vertex.getJobvertexId();
			VertexCheckpointTracker vertexTracker = vertexTrackers.get(vertexId);
			if (vertexTracker != null) {
				vertexTracker.collectTaskTrace(subtaskIndex, taskCheckpointTrace);
			}

			return TaskAcknowledgeResult.SUCCESS;
		}
	}

	/**
	 * Adds a master state (state generated on the checkpoint coordinator) to
	 * the pending checkpoint.
	 *
	 * @param state The state to add
	 */
	public void addMasterState(MasterState state) {
		checkNotNull(state);

		synchronized (lock) {
			if (!discarded) {
				masterState.add(state);
			}
		}
	}


	// ------------------------------------------------------------------------
	//  Cancellation
	// ------------------------------------------------------------------------

	/**
	 * Aborts a checkpoint because it expired (took too long).
	 */
	public void abortExpired() {
		abortWithCause(new Exception("Checkpoint expired before completing"));
	}

	/**
	 * Aborts the pending checkpoint because a newer completed checkpoint subsumed it.
	 */
	public void abortSubsumed() {
		if (props.forceCheckpoint()) {
			throw new IllegalStateException("Bug: forced checkpoints must never be subsumed");
		}

		abortWithCause(new Exception("Checkpoint has been subsumed"));
	}


	public void abortDeclined() {
		abortWithCause(new Exception("Checkpoint was declined (tasks not ready)"));
	}

	/**
	 * Aborts the pending checkpoint due to an error.
	 * @param cause The error's exception.
	 */
	public void abortError(@Nonnull Throwable cause) {
		abortWithCause(new Exception("Checkpoint failed: " + cause.getMessage(), cause));
	}

	private void abortWithCause(@Nonnull Exception cause) {
		try {
			checkpointStatus = CheckpointStatus.FAILED;
			failure = ExceptionUtils.stringifyException(cause);
			terminateTimestamp = System.currentTimeMillis();
			onCompletionPromise.completeExceptionally(cause);
		} finally {
			dispose(true);
		}
	}

	private void dispose(boolean releaseState) {

		synchronized (lock) {
			try {
				numAcknowledgedTasks = -1;
				if (!discarded && releaseState) {
					executor.execute(new Runnable() {
						@Override
						public void run() {

							// discard the private states.
							// unregistered shared states are still considered private at this point.
							try {
								StateUtil.bestEffortDiscardAllStateObjects(operatorStates.values());
								targetLocation.disposeOnFailure();
							} catch (Throwable t) {
								LOG.warn("Could not properly dispose the private states in the pending checkpoint {} of job {}.",
									checkpointId, jobId, t);
							} finally {
								operatorStates.clear();
							}
						}
					});

				}
			} finally {
				discarded = true;
				notYetAcknowledgedTasks.clear();
				acknowledgedTasks.clear();
				cancelCanceller();
			}
		}
	}

	private void cancelCanceller() {
		try {
			final ScheduledFuture<?> canceller = this.cancellerHandle;
			if (canceller != null) {
				canceller.cancel(false);
			}
		}
		catch (Exception e) {
			// this code should not throw exceptions
			LOG.warn("Error while cancelling checkpoint timeout task", e);
		}
	}


	CheckpointTrace getCheckpointTrace() {
		synchronized (lock) {
			long duration = terminateTimestamp > 0 ?
				terminateTimestamp - checkpointTimestamp :
				System.currentTimeMillis() - checkpointTimestamp;

			Map<JobVertexID, VertexCheckpointTrace> vertexTraces = new HashMap<>();
			for (Map.Entry<JobVertexID, VertexCheckpointTracker> vertexEntry : vertexTrackers.entrySet()) {
				JobVertexID vertexId = vertexEntry.getKey();
				VertexCheckpointTracker vertexTracker = vertexEntry.getValue();
				VertexCheckpointTrace vertexTrace = vertexTracker.getVertexTrace();
				vertexTraces.put(vertexId, vertexTrace);
			}

			return new CheckpointTrace(
				checkpointId,
				props.getCheckpointType(),
				checkpointStatus,
				checkpointTimestamp,
				terminateTimestamp,
				duration,
				failure,
				vertexTraces
			);
		}
	}

	// ------------------------------------------------------------------------
	//  Utilities
	// ------------------------------------------------------------------------

	@Override
	public String toString() {
		return String.format("Pending Checkpoint %d @ %d - confirmed=%d, pending=%d",
				checkpointId, checkpointTimestamp, getNumberOfAcknowledgedTasks(), getNumberOfNonAcknowledgedTasks());
	}
}
