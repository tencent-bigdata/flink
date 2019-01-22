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

import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.rest.messages.json.ExecutionAttemptIDDeserializer;
import org.apache.flink.runtime.rest.messages.json.ExecutionAttemptIDSerializer;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

/**
 * The information for an exception.
 */
public class ExceptionInfo {

	private static final String FIELD_NAME_TIME  = "time";
	private static final String FIELD_NAME_FAILURE = "failure";
	private static final String FIELD_NAME_EXECUTION_ID = "executionId";
	private static final String FIELD_NAME_TASK_NAME = "taskName";
	private static final String FIELD_NAME_ATTEMPT_NUMBER = "attemptNumber";
	private static final String FIELD_NAME_EXECUTOR = "executor";

	@JsonProperty(FIELD_NAME_TIME)
	private final long time;

	@JsonProperty(FIELD_NAME_FAILURE)
	private final String failure;

	@JsonProperty(FIELD_NAME_EXECUTION_ID)
	@JsonSerialize(using = ExecutionAttemptIDSerializer.class)
	private final ExecutionAttemptID executionId;

	@JsonProperty(FIELD_NAME_TASK_NAME)
	private final String taskName;

	@JsonProperty(FIELD_NAME_ATTEMPT_NUMBER)
	private final int attemptNumber;

	@JsonProperty(FIELD_NAME_EXECUTOR)
	private final ExecutorInfo executorInfo;

	@JsonCreator
	public ExceptionInfo(
		@JsonProperty(FIELD_NAME_TIME) long time,
		@JsonProperty(FIELD_NAME_FAILURE) String failure,
		@JsonProperty(FIELD_NAME_EXECUTION_ID) @JsonDeserialize(using = ExecutionAttemptIDDeserializer.class) ExecutionAttemptID executionId,
		@JsonProperty(FIELD_NAME_TASK_NAME) String taskName,
		@JsonProperty(FIELD_NAME_ATTEMPT_NUMBER) int attemptNumber,
		@JsonProperty(FIELD_NAME_EXECUTOR) ExecutorInfo executorInfo
	) {
		this.time = time;
		this.failure = failure;
		this.executionId = executionId;
		this.taskName = taskName;
		this.attemptNumber = attemptNumber;
		this.executorInfo = executorInfo;
	}

	public long getTime() {
		return time;
	}

	public String getFailure() {
		return failure;
	}

	public ExecutionAttemptID getExecutionId() {
		return executionId;
	}

	public String getTaskName() {
		return taskName;
	}

	public int getAttemptNumber() {
		return attemptNumber;
	}

	public ExecutorInfo getExecutorInfo() {
		return executorInfo;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ExceptionInfo that = (ExceptionInfo) o;
		return time == that.time &&
			Objects.equals(failure, that.failure) &&
			Objects.equals(executionId, that.executionId) &&
			attemptNumber == that.attemptNumber &&
			Objects.equals(taskName, that.taskName) &&
			Objects.equals(executorInfo, that.executorInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(time, failure, executionId, taskName, attemptNumber, executorInfo);
	}

	@Override
	public String toString() {
		return "ExceptionInfo{" +
			"time=" + time +
			", failure='" + failure + '\'' +
			", executionId=" + executionId +
			", taskName='" + taskName + '\'' +
			", attemptNumber=" + attemptNumber +
			", executorInfo=" + executorInfo +
			'}';
	}
}
