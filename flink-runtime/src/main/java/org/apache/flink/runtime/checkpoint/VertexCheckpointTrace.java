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

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The trace for a vertex's checkpoint operation.
 */
public class VertexCheckpointTrace implements Serializable {

	private final Set<Integer> unacknowledgedTasks;

	private final Map<Integer, TaskCheckpointTrace> acknowledgedTaskTraces;

	public VertexCheckpointTrace(
		Set<Integer> unacknowledgedTasks,
		Map<Integer, TaskCheckpointTrace> acknowledgedTaskTraces
	) {
		Preconditions.checkNotNull(unacknowledgedTasks);
		Preconditions.checkNotNull(acknowledgedTaskTraces);

		this.unacknowledgedTasks = unacknowledgedTasks;
		this.acknowledgedTaskTraces = acknowledgedTaskTraces;
	}

	public Set<Integer> getUnacknowledgedTasks() {
		return unacknowledgedTasks;
	}

	public Map<Integer, TaskCheckpointTrace> getAcknowledgedTaskTraces() {
		return acknowledgedTaskTraces;
	}

	public int getNumTasks() {
		return unacknowledgedTasks.size() + acknowledgedTaskTraces.size();
	}

	public int getNumAcknowledgedTasks() {
		return acknowledgedTaskTraces.size();
	}

	public long getSize() {
		long size = 0;
		for (TaskCheckpointTrace taskTrace : acknowledgedTaskTraces.values()) {
			size += taskTrace.getSize();
		}

		return size;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		VertexCheckpointTrace that = (VertexCheckpointTrace) o;
		return Objects.equals(unacknowledgedTasks, that.unacknowledgedTasks) &&
			Objects.equals(acknowledgedTaskTraces, that.acknowledgedTaskTraces);
	}

	@Override
	public int hashCode() {
		return Objects.hash(unacknowledgedTasks, acknowledgedTaskTraces);
	}

	@Override
	public String toString() {
		return "VertexCheckpointTrace{" +
			"unacknowledgedTasks=" + unacknowledgedTasks +
			", acknowledgedTaskTraces=" + acknowledgedTaskTraces +
			'}';
	}
}
