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
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.rest.messages.json.ExecutionAttemptIDDeserializer;
import org.apache.flink.runtime.rest.messages.json.ExecutionAttemptIDSerializer;
import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

/**
 * The information for an execution.
 */
public class ExecutionInfo {

	private static final String FIELD_NAME_ID = "id";
	private static final String FIELD_NAME_ATTEMPT_NUMBER = "attemptNumber";
	private static final String FIELD_NAME_START_TIME = "startTime";
	private static final String FIELD_NAME_END_TIME = "endTime";
	private static final String FIELD_NAME_DURATION = "duration";
	private static final String FIELD_NAME_EXECUTOR = "executor";
	private static final String FIELD_NAME_FAILURE = "failure";
	private static final String FIELD_NAME_STATUS = "status";
	private static final String FIELD_NAME_METRICS = "metrics";

	@JsonProperty(FIELD_NAME_ID)
	@JsonSerialize(using = ExecutionAttemptIDSerializer.class)
	private final ExecutionAttemptID id;

	@JsonProperty(FIELD_NAME_ATTEMPT_NUMBER)
	private final int attemptNumber;

	@JsonProperty(FIELD_NAME_START_TIME)
	private final long startTime;

	@JsonProperty(FIELD_NAME_END_TIME)
	private final long endTime;

	@JsonProperty(FIELD_NAME_DURATION)
	private final long duration;

	@JsonProperty(FIELD_NAME_EXECUTOR)
	private final ExecutorInfo executor;

	@JsonProperty(FIELD_NAME_FAILURE)
	private final String failure;

	@JsonProperty(FIELD_NAME_STATUS)
	private final ExecutionState status;

	@JsonProperty(FIELD_NAME_METRICS)
	private final ExecutionMetricsInfo metrics;

	@JsonCreator
	public ExecutionInfo(
		@JsonProperty(FIELD_NAME_ID) @JsonDeserialize(using = ExecutionAttemptIDDeserializer.class) ExecutionAttemptID id,
		@JsonProperty(FIELD_NAME_ATTEMPT_NUMBER) int attemptNumber,
		@JsonProperty(FIELD_NAME_START_TIME) long startTime,
		@JsonProperty(FIELD_NAME_END_TIME) long endTime,
		@JsonProperty(FIELD_NAME_DURATION) long duration,
		@JsonProperty(FIELD_NAME_EXECUTOR) ExecutorInfo executor,
		@JsonProperty(FIELD_NAME_FAILURE) String failure,
		@JsonProperty(FIELD_NAME_STATUS) ExecutionState status,
		@JsonProperty(FIELD_NAME_METRICS) ExecutionMetricsInfo metrics
	) {
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(metrics);

		this.id = id;
		this.attemptNumber = attemptNumber;
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.executor = executor;
		this.failure = failure;
		this.status = status;
		this.metrics = metrics;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ExecutionInfo that = (ExecutionInfo) o;
		return Objects.equals(id, that.id) &&
			attemptNumber == that.attemptNumber &&
			startTime == that.startTime &&
			endTime == that.endTime &&
			duration == that.duration &&
			Objects.equals(executor, that.executor) &&
			Objects.equals(failure, that.failure) &&
			status == that.status &&
			Objects.equals(metrics, that.metrics);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, attemptNumber, startTime, endTime, duration, executor, failure, status, metrics);
	}

	@Override
	public String toString() {
		return "ExecutionInfo{" +
			"id=" + id +
			", attemptNumber=" + attemptNumber +
			", startTime=" + startTime +
			", endTime=" + endTime +
			", duration=" + duration +
			", executor=" + executor +
			", failure=" + failure +
			", status=" + status +
			", metrics=" + metrics +
			'}';
	}
}
