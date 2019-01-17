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

import org.apache.flink.util.Preconditions;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * A snapshot of the checkpoint traces.
 */
public class CheckpointTracesSnapshot implements Serializable {

	private static final long serialVersionUID = 8914278419087217964L;

	private final int numPendingCheckpoints;

	private final int numCompletedCheckpoints;

	private final int numFailedCheckpoints;

	@Nullable
	private final CheckpointTrace lastCompletedCheckpointTrace;

	@Nullable
	private final CheckpointTrace lastFailedCheckpointTrace;

	private final Map<Long, CheckpointTrace> checkpointTraces;

	public CheckpointTracesSnapshot(
		int numPendingCheckpoints,
		int numCompletedCheckpoints,
		int numFailedCheckpoints,
		CheckpointTrace lastCompletedCheckpointTrace,
		CheckpointTrace lastFailedCheckpointTrace,
		Map<Long, CheckpointTrace> checkpointTraces
	) {
		Preconditions.checkNotNull(checkpointTraces);

		this.numPendingCheckpoints = numPendingCheckpoints;
		this.numCompletedCheckpoints = numCompletedCheckpoints;
		this.numFailedCheckpoints = numFailedCheckpoints;
		this.lastCompletedCheckpointTrace = lastCompletedCheckpointTrace;
		this.lastFailedCheckpointTrace = lastFailedCheckpointTrace;
		this.checkpointTraces = checkpointTraces;
	}

	public int getNumPendingCheckpoints() {
		return numPendingCheckpoints;
	}

	public int getNumCompletedCheckpoints() {
		return numCompletedCheckpoints;
	}

	public int getNumFailedCheckpoints() {
		return numFailedCheckpoints;
	}

	public CheckpointTrace getLastCompletedCheckpointTrace() {
		return lastCompletedCheckpointTrace;
	}

	public CheckpointTrace getLastFailedCheckpointTrace() {
		return lastFailedCheckpointTrace;
	}

	public Map<Long, CheckpointTrace> getCheckpointTraces() {
		return checkpointTraces;
	}

	public CheckpointTrace getCheckpointTrace(long checkpointId) {
		CheckpointTrace CheckpointTrace = checkpointTraces.get(checkpointId);
		if (CheckpointTrace != null) {
			return CheckpointTrace;
		}

		if (lastCompletedCheckpointTrace != null && lastCompletedCheckpointTrace.getCheckpointId() == checkpointId) {
			return lastCompletedCheckpointTrace;
		}

		if (lastFailedCheckpointTrace != null && lastFailedCheckpointTrace.getCheckpointId() == checkpointId) {
			return lastFailedCheckpointTrace;
		}

		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CheckpointTracesSnapshot that = (CheckpointTracesSnapshot) o;
		return numCompletedCheckpoints == that.numCompletedCheckpoints &&
			numFailedCheckpoints == that.numFailedCheckpoints &&
			Objects.equals(lastCompletedCheckpointTrace, that.lastCompletedCheckpointTrace) &&
			Objects.equals(lastFailedCheckpointTrace, that.lastFailedCheckpointTrace) &&
			Objects.equals(checkpointTraces, that.checkpointTraces);
	}

	@Override
	public int hashCode() {
		return Objects.hash(numCompletedCheckpoints, numFailedCheckpoints,
			lastCompletedCheckpointTrace, lastFailedCheckpointTrace, checkpointTraces);
	}
}
