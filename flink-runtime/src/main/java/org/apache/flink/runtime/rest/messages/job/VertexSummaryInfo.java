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

package org.apache.flink.runtime.rest.messages.job;

import org.apache.flink.runtime.execution.ExecutionState;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.rest.messages.json.JobVertexIDDeserializer;
import org.apache.flink.runtime.rest.messages.json.JobVertexIDSerializer;
import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;
import java.util.Objects;

/**
 * The basic information for a vertex.
 */
public class VertexSummaryInfo {

	private static final String FIELD_NAME_ID = "id";
	private static final String FIELD_NAME_TOPOLOGY_ID = "topologyId";
	private static final String FIELD_NAME_NAME = "name";
	private static final String FIELD_NAME_START_TIME = "startTime";
	private static final String FIELD_NAME_END_TIME = "endTime";
	private static final String FIELD_NAME_DURATION = "duration";
	private static final String FIELD_NAME_PARALLELISM = "parallelism";
	private static final String FIELD_NAME_MAX_PARALLELISM = "maxParallelism";
	private static final String FIELD_NAME_NUM_TASKS_PER_STATE = "numTasksPerState";
	private static final String FIELD_NAME_STATUS = "status";
	private static final String FIELD_NAME_METRICS = "metrics";

	@JsonProperty(FIELD_NAME_ID)
	@JsonSerialize(using = JobVertexIDSerializer.class)
	private final JobVertexID id;

	@JsonProperty(FIELD_NAME_TOPOLOGY_ID)
	private final int topologyId;

	@JsonProperty(FIELD_NAME_NAME)
	private final String name;

	@JsonProperty(FIELD_NAME_START_TIME)
	private final long startTime;

	@JsonProperty(FIELD_NAME_END_TIME)
	private final long endTime;

	@JsonProperty(FIELD_NAME_DURATION)
	private final long duration;

	@JsonProperty(FIELD_NAME_PARALLELISM)
	private final int parallelism;

	@JsonProperty(FIELD_NAME_MAX_PARALLELISM)
	private final int maxParallelism;

	@JsonProperty(FIELD_NAME_NUM_TASKS_PER_STATE)
	private final Map<ExecutionState, Integer> numTasksPerState;

	@JsonProperty(FIELD_NAME_STATUS)
	private final ExecutionState status;

	@JsonProperty(FIELD_NAME_METRICS)
	private final ExecutionMetricsSummaryInfo metrics;

	@JsonCreator
	public VertexSummaryInfo(
		@JsonProperty(FIELD_NAME_ID) @JsonDeserialize(using = JobVertexIDDeserializer.class) JobVertexID id,
		@JsonProperty(FIELD_NAME_TOPOLOGY_ID) int topologyId,
		@JsonProperty(FIELD_NAME_NAME) String name,
		@JsonProperty(FIELD_NAME_START_TIME) long startTime,
		@JsonProperty(FIELD_NAME_END_TIME) long endTime,
		@JsonProperty(FIELD_NAME_DURATION) long duration,
		@JsonProperty(FIELD_NAME_PARALLELISM) int parallelism,
		@JsonProperty(FIELD_NAME_MAX_PARALLELISM) int maxParallelism,
		@JsonProperty(FIELD_NAME_NUM_TASKS_PER_STATE) Map<ExecutionState, Integer> numTasksPerState,
		@JsonProperty(FIELD_NAME_STATUS) ExecutionState status,
		@JsonProperty(FIELD_NAME_METRICS) ExecutionMetricsSummaryInfo metrics
	) {
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(numTasksPerState);
		Preconditions.checkNotNull(metrics);

		this.id = id;
		this.topologyId = topologyId;
		this.name = name;
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.parallelism = parallelism;
		this.maxParallelism = maxParallelism;
		this.numTasksPerState = numTasksPerState;
		this.status = status;
		this.metrics = metrics;
	}

	public JobVertexID getId() {
		return id;
	}

	public int getTopologyId() {
		return topologyId;
	}

	public String getName() {
		return name;
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

	public int getParallelism() {
		return parallelism;
	}

	public int getMaxParallelism() {
		return maxParallelism;
	}

	public Map<ExecutionState, Integer> getNumTasksPerState() {
		return numTasksPerState;
	}

	public ExecutionState getStatus() {
		return status;
	}

	public ExecutionMetricsSummaryInfo getMetrics() {
		return metrics;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		VertexSummaryInfo that = (VertexSummaryInfo) o;
		return Objects.equals(id, that.id) &&
			topologyId == that.topologyId &&
			Objects.equals(name, that.name) &&
			startTime == that.startTime &&
			endTime == that.endTime &&
			duration == that.duration &&
			parallelism == that.parallelism &&
			maxParallelism == that.maxParallelism &&
			Objects.equals(numTasksPerState, that.numTasksPerState) &&
			status == that.status &&
			Objects.equals(metrics, that.metrics);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, topologyId, name, startTime, endTime, duration, parallelism, maxParallelism, numTasksPerState, status, metrics);
	}

	@Override
	public String toString() {
		return "VertexSummaryInfo{" +
			"id=" + id +
			", topologyId=" + topologyId +
			", name='" + name + '\'' +
			", startTime=" + startTime +
			", endTime=" + endTime +
			", duration=" + duration +
			", parallelism=" + parallelism +
			", maxParallelism=" + maxParallelism +
			", numTasksPerState=" + numTasksPerState +
			", status=" + status +
			", metrics=" + metrics +
			'}';
	}
}
