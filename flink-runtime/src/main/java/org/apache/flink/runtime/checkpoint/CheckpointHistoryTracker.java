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

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The tracker for checkpoint history.
 */
public class CheckpointHistoryTracker {

	private final int numRememberedCheckpoints;

	private volatile int numCompletedCheckpoints;

	private volatile int numFailedCheckpoints;

	private volatile CheckpointTrace lastFailedCheckpointTrace;

	private volatile CheckpointTrace lastCompletedCheckpointTrace;

	private final SortedMap<Long, CheckpointTrace> terminateCheckpointTraces;

	public CheckpointHistoryTracker(int numRememberedCheckpoints) {
		this.numRememberedCheckpoints = numRememberedCheckpoints;
		this.numCompletedCheckpoints = 0;
		this.numFailedCheckpoints = 0;
		this.lastCompletedCheckpointTrace = null;
		this.lastFailedCheckpointTrace = null;
		this.terminateCheckpointTraces = new TreeMap<>();
	}

	public void addCheckpointTrace(CheckpointTrace checkpointTrace) {
		Preconditions.checkNotNull(checkpointTrace);

		if (checkpointTrace.getCheckpointStatus() == CheckpointStatus.COMPLETED) {
			numCompletedCheckpoints++;

			if (lastCompletedCheckpointTrace == null ||
				checkpointTrace.getCheckpointId() > lastCompletedCheckpointTrace.getCheckpointId()) {
				lastCompletedCheckpointTrace = checkpointTrace;
			}
		} else if (checkpointTrace.getCheckpointStatus() == CheckpointStatus.FAILED) {
			numFailedCheckpoints++;

			if (lastFailedCheckpointTrace == null ||
				checkpointTrace.getCheckpointId() > lastFailedCheckpointTrace.getCheckpointId()) {
				lastFailedCheckpointTrace = checkpointTrace;
			}
		} else {
			throw new IllegalStateException();
		}

		terminateCheckpointTraces.put(checkpointTrace.getCheckpointId(), checkpointTrace);
		if (terminateCheckpointTraces.size() > numRememberedCheckpoints) {
			terminateCheckpointTraces.remove(terminateCheckpointTraces.firstKey());
		}
	}

	public int getNumCompletedCheckpoints() {
		return numCompletedCheckpoints;
	}

	public int getNumFailedCheckpoints() {
		return numFailedCheckpoints;
	}

	public CheckpointTrace getLastFailedCheckpointTrace() {
		return lastFailedCheckpointTrace;
	}

	public CheckpointTrace getLastCompletedCheckpointTrace() {
		return lastCompletedCheckpointTrace;
	}

	public SortedMap<Long, CheckpointTrace> getCheckpointTraces() {
		return terminateCheckpointTraces;
	}
}
