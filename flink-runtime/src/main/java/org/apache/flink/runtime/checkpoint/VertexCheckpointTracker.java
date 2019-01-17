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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The tracker for a vertex's checkpoint operation.
 */
public class VertexCheckpointTracker {

	private final Set<Integer> unacknowledgedTasks;

	private final Map<Integer, TaskCheckpointTrace> acknowledgedTaskTrackers;

	public VertexCheckpointTracker(
		Set<Integer> tasksToAcknowledge
	) {
		Preconditions.checkNotNull(tasksToAcknowledge);

		this.unacknowledgedTasks = tasksToAcknowledge;
		this.acknowledgedTaskTrackers = new HashMap<>();
	}

	public void collectTaskTrace(int taskIndex, TaskCheckpointTrace taskTrace) {
		Preconditions.checkNotNull(taskTrace);

		if (unacknowledgedTasks.remove(taskIndex)) {
			acknowledgedTaskTrackers.put(taskIndex, taskTrace);
		}
	}

	public VertexCheckpointTrace getVertexTrace() {

		Set<Integer> unacknowledgedTasksCopy = new HashSet<>(unacknowledgedTasks);
		Map<Integer, TaskCheckpointTrace> acknowledgedTaskTracesCopy = new HashMap<>(acknowledgedTaskTrackers);

		return new VertexCheckpointTrace(unacknowledgedTasksCopy, acknowledgedTaskTracesCopy);
	}

	public Set<Integer> getUnacknowledgedTasks() {
		return unacknowledgedTasks;
	}

	public Map<Integer, TaskCheckpointTrace> getAcknowledgedTaskTrackers() {
		return acknowledgedTaskTrackers;
	}

	public int getNumTasks() {
		return unacknowledgedTasks.size() + acknowledgedTaskTrackers.size();
	}

	public int getNumAcknowledgedTasks() {
		return acknowledgedTaskTrackers.size();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		VertexCheckpointTracker that = (VertexCheckpointTracker) o;
		return Objects.equals(unacknowledgedTasks, that.unacknowledgedTasks) &&
			Objects.equals(acknowledgedTaskTrackers, that.acknowledgedTaskTrackers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(unacknowledgedTasks, acknowledgedTaskTrackers);
	}

	@Override
	public String toString() {
		return "VertexCheckpointTracker{" +
			"unacknowledgedTasks=" + unacknowledgedTasks +
			", acknowledgedTaskTrackers=" + acknowledgedTaskTrackers +
			'}';
	}
}
