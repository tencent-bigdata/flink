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

package org.apache.flink.runtime.rest.messages.job.checkpoints;

import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.rest.messages.json.JobVertexIDDeserializer;
import org.apache.flink.runtime.rest.messages.json.JobVertexIDSerializer;
import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.Objects;

/**
 * The summary information for a vertex's checkpoint.
 */
public class VertexCheckpointSummaryInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String FIELD_NAME_VERTEX_ID = "vertexId";
	private static final String FIELD_NAME_NUM_TASKS = "numTasks";
	private static final String FIELD_NAME_NUM_ACKNOWLEDGED_TASKS = "numAcknowledgedTasks";
	private static final String FIELD_NAME_METRICS = "metrics";

	@JsonProperty(FIELD_NAME_VERTEX_ID)
	@JsonSerialize(using = JobVertexIDSerializer.class)
	private final JobVertexID vertexId;

	@JsonProperty(FIELD_NAME_NUM_TASKS)
	private final int numTasks;

	@JsonProperty(FIELD_NAME_NUM_ACKNOWLEDGED_TASKS)
	private final int numAcknowledgedTasks;

	@JsonProperty(FIELD_NAME_METRICS)
	private final CheckpointMetricsSummaryInfo metrics;

	@JsonCreator
	public VertexCheckpointSummaryInfo(
		@JsonProperty(FIELD_NAME_VERTEX_ID) @JsonDeserialize(using = JobVertexIDDeserializer.class) JobVertexID vertexId,
		@JsonProperty(FIELD_NAME_NUM_TASKS) int numTasks,
		@JsonProperty(FIELD_NAME_NUM_ACKNOWLEDGED_TASKS) int numAcknowledgedTasks,
		@JsonProperty(FIELD_NAME_METRICS) CheckpointMetricsSummaryInfo metrics
	) {
		Preconditions.checkNotNull(vertexId);

		this.vertexId = vertexId;
		this.numTasks = numTasks;
		this.numAcknowledgedTasks = numAcknowledgedTasks;
		this.metrics = metrics;
	}

	public JobVertexID getVertexId() {
		return vertexId;
	}

	public int getNumTasks() {
		return numTasks;
	}

	public int getNumAcknowledgedTasks() {
		return numAcknowledgedTasks;
	}

	public CheckpointMetricsSummaryInfo getMetrics() {
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

		VertexCheckpointSummaryInfo that = (VertexCheckpointSummaryInfo) o;
		return Objects.equals(vertexId, that.vertexId) &&
			numTasks == that.numTasks &&
			numAcknowledgedTasks == that.numAcknowledgedTasks &&
			Objects.equals(metrics, that.metrics);
	}

	@Override
	public int hashCode() {
		return Objects.hash(vertexId, numTasks, numAcknowledgedTasks, metrics);
	}

	@Override
	public String toString() {
		return "VertexCheckpointSummaryInfo{" +
			", vertexId=" + vertexId +
			", numTasks=" + numTasks +
			", numAcknowledgedTasks=" + numAcknowledgedTasks +
			", metrics=" + metrics +
			'}';
	}
}
