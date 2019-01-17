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

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * The information for a task's checkpoint operation.
 */
public class TaskCheckpointInfo implements Serializable {

	private static final String FIELD_NAME_INDEX = "index";
	private static final String FIELD_NAME_START_TIME = "startTime";
	private static final String FIELD_NAME_END_TIME = "endTime";
	private static final String FIELD_NAME_METRICS = "metrics";

	@JsonProperty(FIELD_NAME_INDEX)
	private final int index;

	@JsonProperty(FIELD_NAME_START_TIME)
	private final long startTime;

	@JsonProperty(FIELD_NAME_END_TIME)
	private final long endTime;

	@JsonProperty(FIELD_NAME_METRICS)
	private final CheckpointMetricsInfo metrics;

	@JsonCreator
	public TaskCheckpointInfo(
		@JsonProperty(FIELD_NAME_INDEX) int index,
		@JsonProperty(FIELD_NAME_START_TIME) long startTime,
		@JsonProperty(FIELD_NAME_END_TIME) long endTime,
		@JsonProperty(FIELD_NAME_METRICS) CheckpointMetricsInfo metrics
	) {
		this.index = index;
		this.startTime = startTime;
		this.endTime = endTime;
		this.metrics = metrics;
	}

	public TaskCheckpointInfo(int index) {
		this(index, -1, -1, new CheckpointMetricsInfo());
	}

	public int getIndex() {
		return index;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public CheckpointMetricsInfo getMetrics() {
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

		TaskCheckpointInfo that = (TaskCheckpointInfo) o;
		return index == that.index &&
			startTime == that.startTime &&
			endTime == that.endTime &&
			Objects.equals(metrics, that.metrics);
	}

	@Override
	public int hashCode() {
		return Objects.hash(index, startTime, endTime, metrics);
	}

	@Override
	public String toString() {
		return "TaskCheckpointInfo{" +
			"index=" + index +
			", startTime=" + startTime +
			", endTime=" + endTime +
			", metrics=" + metrics +
			'}';
	}
}
