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

package org.apache.flink.runtime.taskmanager;

import org.apache.flink.runtime.accumulators.AccumulatorSnapshot;
import org.apache.flink.runtime.execution.ExecutionState;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.executiongraph.IOMetrics;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.util.SerializedThrowable;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class represents an update about a task's execution state.
 *
 * <b>NOTE:</b> The exception that may be attached to the state update is
 * not necessarily a Flink or core Java exception, but may be an exception
 * from the user code. As such, it cannot be deserialized without a
 * special class loader. For that reason, the class keeps the actual
 * exception field transient and deserialized it lazily, with the
 * appropriate class loader.
 */
public class TaskExecutionState implements Serializable {

	private static final long serialVersionUID = 1L;

	private final JobID jobID;

	private final ExecutionAttemptID executionId;

	private final JobVertexID vertexId;

	private final int subtaskIndex;

	private final int attemptNumber;

	private final ExecutionState executionState;

	private final SerializedThrowable throwable;

	/** Serialized user-defined accumulators */
	private final AccumulatorSnapshot accumulators;

	private final IOMetrics ioMetrics;

	/**
	 * Creates a new task execution state update, with no attached exception and no accumulators.
	 *
	 * @param jobID
	 *        the ID of the job the task belongs to
	 * @param executionId
	 *        the ID of the task execution whose state is to be reported
	 * @param vertexId
	 *        the ID of the vertex the task belongs to
	 * @param subtaskIndex
	 *        the index of the task
	 * @param attemptNumber
	 *        the attempt number of the task execution
	 * @param executionState
	 *        the execution state to be reported
	 */
	public TaskExecutionState(
		JobID jobID,
		ExecutionAttemptID executionId,
		JobVertexID vertexId,
		int subtaskIndex,
		int attemptNumber,
		ExecutionState executionState
	) {
		this(jobID, executionId, vertexId, subtaskIndex, attemptNumber, executionState, null, null, null);
	}

	/**
	 * Creates a new task execution state update, with an attached exception but no accumulators.
	 *
	 * @param jobID
	 *        the ID of the job the task belongs to
	 * @param executionId
	 *        the ID of the task execution whose state is to be reported
	 * @param vertexId
	 *        the ID of the vertex the task belongs to
	 * @param subtaskIndex
	 *        the index of the task
	 * @param attemptNumber
	 *        the attempt number of the task execution
	 * @param executionState
	 *        the execution state to be reported
	 */
	public TaskExecutionState(
		JobID jobID,
		ExecutionAttemptID executionId,
		JobVertexID vertexId,
		int subtaskIndex,
		int attemptNumber,
		ExecutionState executionState,
		Throwable error
	) {
		this(jobID, executionId, vertexId, subtaskIndex, attemptNumber, executionState, error, null, null);
	}

	/**
	 * Creates a new task execution state update, with an attached exception.
	 * This constructor may never throw an exception.
	 *
	 * @param jobID
	 *        the ID of the job the task belongs to
	 * @param executionId
	 *        the ID of the task execution whose state is to be reported
	 * @param vertexId
	 *        the ID of the vertex the task belongs to
	 * @param subtaskIndex
	 *        the index of the task
	 * @param attemptNumber
	 *        the attempt number of the task execution
	 * @param executionState
	 *        the execution state to be reported
	 * @param error
	 *        an optional error
	 * @param accumulators
	 *        The flink and user-defined accumulators which may be null.
	 */
	public TaskExecutionState(
		JobID jobID,
		ExecutionAttemptID executionId,
		JobVertexID vertexId,
		int subtaskIndex,
		int attemptNumber,
		ExecutionState executionState,
		Throwable error,
		AccumulatorSnapshot accumulators,
		IOMetrics ioMetrics
	) {

		if (jobID == null || executionId == null || executionState == null) {
			throw new NullPointerException();
		}

		this.jobID = jobID;
		this.executionId = executionId;
		this.vertexId = vertexId;
		this.subtaskIndex = subtaskIndex;
		this.attemptNumber = attemptNumber;
		this.executionState = executionState;
		if (error != null) {
			this.throwable = new SerializedThrowable(error);
		} else {
			this.throwable = null;
		}
		this.accumulators = accumulators;
		this.ioMetrics = ioMetrics;
	}

	// --------------------------------------------------------------------------------------------

	/**
	 * Gets the attached exception, which is in serialized form. Returns null,
	 * if the status update is no failure with an associated exception.
	 * 
	 * @param userCodeClassloader The classloader that can resolve user-defined exceptions.
	 * @return The attached exception, or null, if none.
	 */
	public Throwable getError(ClassLoader userCodeClassloader) {
		if (this.throwable == null) {
			return null;
		}
		else {
			return this.throwable.deserializeError(userCodeClassloader);
		}
	}

	/**
	 * Returns the ID of the task this result belongs to
	 * 
	 * @return the ID of the task this result belongs to
	 */
	public ExecutionAttemptID getID() {
		return this.executionId;
	}

	/**
	 * Returns the ID of the vertex this task belongs to
	 *
	 * @return the ID of the vertex this task belongs to
	 */
	public JobVertexID getVertexId() {
		return vertexId;
	}

	/**
	 * Returns the index of the task.
	 *
	 * @return the index of the task.
	 */
	public int getSubtaskIndex() {
		return subtaskIndex;
	}

	/**
	 * Returns the attempt number of this execution.
	 *
	 * @return The attempt number of this execution.
	 */
	public int getAttemptNumber() {
		return attemptNumber;
	}

	/**
	 * Returns the new execution state of the task.
	 * 
	 * @return the new execution state of the task
	 */
	public ExecutionState getExecutionState() {
		return this.executionState;
	}

	/**
	 * The ID of the job the task belongs to
	 * 
	 * @return the ID of the job the task belongs to
	 */
	public JobID getJobID() {
		return this.jobID;
	}

	/**
	 * Gets flink and user-defined accumulators in serialized form.
	 */
	public AccumulatorSnapshot getAccumulators() {
		return accumulators;
	}

	public IOMetrics getIOMetrics() {
		return ioMetrics;
	}

	// --------------------------------------------------------------------------------------------


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TaskExecutionState that = (TaskExecutionState) o;

		return Objects.equals(jobID, that.jobID) &&
			Objects.equals(executionId, that.executionId) &&
			Objects.equals(vertexId, that.vertexId) &&
			subtaskIndex == that.subtaskIndex &&
			attemptNumber == that.attemptNumber &&
			executionState == that.executionState &&
			(throwable == null) == (that.throwable == null);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jobID, executionId, vertexId, subtaskIndex, attemptNumber, executionState);
	}

	@Override
	public String toString() {
		return "TaskExecutionState{" +
			"jobID=" + jobID +
			", executionId=" + executionId +
			", vertexId=" + vertexId +
			", subtaskIndex=" + subtaskIndex +
			", attemptNumber=" + attemptNumber +
			", executionState=" + executionState +
			", throwable=" + throwable +
			", accumulators=" + accumulators +
			", ioMetrics=" + ioMetrics +
			'}';
	}
}
