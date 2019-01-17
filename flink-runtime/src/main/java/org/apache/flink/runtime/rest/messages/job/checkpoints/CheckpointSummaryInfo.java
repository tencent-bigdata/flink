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

import org.apache.flink.runtime.checkpoint.CheckpointStatus;
import org.apache.flink.runtime.checkpoint.CheckpointType;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * The summary information for a checkpoint.
 */
public class CheckpointSummaryInfo {

	private static final String FIELD_NAME_ID = "id";
	private static final String FIELD_NAME_TYPE = "type";
	private static final String FIELD_NAME_STATUS = "status";
	private static final String FIELD_NAME_START_TIME = "startTime";
	private static final String FIELD_NAME_END_TIME = "endTime";
	private static final String FIELD_NAME_DURATION = "duration";
	private static final String FIELD_NAME_NUM_TASKS = "numTasks";
	private static final String FIELD_NAME_NUM_ACKNOWLEDGED_TASKS = "numAcknowledgedTasks";
	private static final String FIELD_NAME_SIZE = "size";
	private static final String FIELD_NAME_FAILURE = "failure";

	@JsonProperty(FIELD_NAME_ID)
	private final long id;

	@JsonProperty(FIELD_NAME_TYPE)
	private final CheckpointType type;

	@JsonProperty(FIELD_NAME_STATUS)
	private final CheckpointStatus status;

	@JsonProperty(FIELD_NAME_START_TIME)
	private final long startTime;

	@JsonProperty(FIELD_NAME_END_TIME)
	private final long endTime;

	@JsonProperty(FIELD_NAME_DURATION)
	private final long duration;

	@JsonProperty(FIELD_NAME_NUM_TASKS)
	private final long numTasks;

	@JsonProperty(FIELD_NAME_NUM_ACKNOWLEDGED_TASKS)
	private final long numAcknowledgedTasks;

	@JsonProperty(FIELD_NAME_SIZE)
	private final long size;

	@JsonProperty(FIELD_NAME_FAILURE)
	private final String failure;

	@JsonCreator
	public CheckpointSummaryInfo(
		@JsonProperty(FIELD_NAME_ID) long id,
		@JsonProperty(FIELD_NAME_TYPE) CheckpointType type,
		@JsonProperty(FIELD_NAME_STATUS) CheckpointStatus status,
		@JsonProperty(FIELD_NAME_START_TIME) long startTime,
		@JsonProperty(FIELD_NAME_END_TIME) long endTime,
		@JsonProperty(FIELD_NAME_DURATION) long duration,
		@JsonProperty(FIELD_NAME_NUM_TASKS) long numTasks,
		@JsonProperty(FIELD_NAME_NUM_ACKNOWLEDGED_TASKS) long numAcknowledgedTasks,
		@JsonProperty(FIELD_NAME_SIZE) long size,
		@JsonProperty(FIELD_NAME_FAILURE) String failure
	) {
		this.id = id;
		this.type = type;
		this.status = status;
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.numTasks = numTasks;
		this.numAcknowledgedTasks = numAcknowledgedTasks;
		this.size = size;
		this.failure = failure;
	}

	public long getId() {
		return id;
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

	public long getNumTasks() {
		return numTasks;
	}

	public long getNumAcknowledgedTasks() {
		return numAcknowledgedTasks;
	}

	public long getSize() {
		return size;
	}

	public String getFailure() {
		return failure;
	}

	public CheckpointStatus getStatus() {
		return status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CheckpointSummaryInfo that = (CheckpointSummaryInfo) o;
		return id == that.id &&
			type == that.type &&
			status == that.status &&
			startTime == that.startTime &&
			endTime == that.endTime &&
			duration == that.duration &&
			numTasks == that.numTasks &&
			numAcknowledgedTasks == that.numAcknowledgedTasks &&
			size == that.size &&
			Objects.equals(failure, that.failure);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, startTime, endTime, duration, numTasks, numAcknowledgedTasks,
			size, failure, status);
	}

	@Override
	public String toString() {
		return "CheckpointSummaryInfo{" +
			"id=" + id +
			", type=" + type +
			", status=" + status +
			", startTime=" + startTime +
			", endTime=" + endTime +
			", duration=" + duration +
			", numTasks=" + numTasks +
			", numAcknowledgedTasks=" + numAcknowledgedTasks +
			", size=" + size +
			", failure='" + failure + '\'' +
			'}';
	}
}
