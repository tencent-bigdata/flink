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

import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.util.Preconditions;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class CheckpointTrace implements Serializable {

	private static final long serialVersionUID = 1L;

	private final long checkpointId;

	private final CheckpointType checkpointType;

	private final CheckpointStatus checkpointStatus;

	private final long startTime;

	private final long endTime;

	private final long duration;

	private final String failure;

	private final Map<JobVertexID, VertexCheckpointTrace> vertexTraces;

	public CheckpointTrace(
		long checkpointId,
		CheckpointType checkpointType,
		CheckpointStatus checkpointStatus,
		long startTime,
		long endTime,
		long duration,
		String failure,
		Map<JobVertexID, VertexCheckpointTrace> vertexTraces
	) {
		Preconditions.checkNotNull(vertexTraces);

		this.checkpointId = checkpointId;
		this.checkpointType = checkpointType;
		this.checkpointStatus = checkpointStatus;
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.failure = failure;
		this.vertexTraces = vertexTraces;
	}

	public long getCheckpointId() {
		return checkpointId;
	}

	public CheckpointType getCheckpointType() {
		return checkpointType;
	}

	public CheckpointStatus getCheckpointStatus() {
		return checkpointStatus;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public long getDuration() {
		return duration;
	}

	public int getNumTasks() {
		int numTasks = 0;
		for (VertexCheckpointTrace vertexCheckpointTrace : vertexTraces.values()) {
			numTasks += vertexCheckpointTrace.getNumTasks();
		}

		return numTasks;
	}

	public int getNumAcknowledgedTasks() {
		int numAcknowledgedTasks = 0;
		for (VertexCheckpointTrace vertexCheckpointTrace : vertexTraces.values()) {
			numAcknowledgedTasks += vertexCheckpointTrace.getNumAcknowledgedTasks();
		}

		return numAcknowledgedTasks;
	}

	public long getSize() {
		long size = 0;
		for (VertexCheckpointTrace vertexCheckpointTrace : vertexTraces.values()) {
			size += vertexCheckpointTrace.getSize();
		}

		return size;
	}

	public String getFailure() {
		return failure;
	}

	public Map<JobVertexID, VertexCheckpointTrace> getVertexTraces() {
		return vertexTraces;
	}

	public VertexCheckpointTrace getVertexTrace(JobVertexID vertexId) {
		return vertexTraces.get(vertexId);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CheckpointTrace that = (CheckpointTrace) o;
		return checkpointId == that.checkpointId &&
			checkpointType == that.checkpointType &&
			checkpointStatus == that.checkpointStatus &&
			startTime == that.startTime &&
			endTime == that.endTime &&
			Objects.equals(failure, that.failure) &&
			Objects.equals(vertexTraces, that.vertexTraces);
	}

	@Override
	public int hashCode() {
		return Objects.hash(checkpointId, checkpointType, checkpointStatus, startTime, endTime,
			failure, vertexTraces);
	}

	@Override
	public String toString() {
		return "CheckpointTrace{" +
			"checkpointId=" + checkpointId +
			", checkpointType=" + checkpointType +
			", checkpointStatus=" + checkpointStatus +
			", startTime=" + startTime +
			", endTime=" + endTime +
			", failure='" + failure + '\'' +
			", vertexTraces=" + vertexTraces +
			'}';
	}
}
