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

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.jobgraph.JobStatus;
import org.apache.flink.runtime.rest.messages.ResponseBody;
import org.apache.flink.runtime.rest.messages.json.JobIDDeserializer;
import org.apache.flink.runtime.rest.messages.json.JobIDSerializer;
import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base class containing information for a job.
 */
public class JobSummaryInfo implements ResponseBody, Serializable {

	private static final long serialVersionUID = 1L;

	public static final String FIELD_NAME_ID = "id";
	public static final String FIELD_NAME_NAME = "name";
	public static final String FIELD_NAME_START_TIME = "startTime";
	public static final String FIELD_NAME_END_TIME = "endTime";
	public static final String FIELD_NAME_DURATION = "duration";
	public static final String FIELD_NAME_PARALLELISM = "parallelism";
	public static final String FIELD_NAME_MAX_PARALLELISM = "maxParallelism";
	public static final String FIELD_NAME_STATUS = "status";

	@JsonProperty(FIELD_NAME_ID)
	@JsonSerialize(using = JobIDSerializer.class)
	private final JobID id;

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

	@JsonProperty(FIELD_NAME_STATUS)
	private final JobStatus status;

	@JsonCreator
	public JobSummaryInfo(
		@JsonDeserialize(using = JobIDDeserializer.class) @JsonProperty(FIELD_NAME_ID) JobID id,
		@JsonProperty(FIELD_NAME_NAME) String name,
		@JsonProperty(FIELD_NAME_START_TIME) long startTime,
		@JsonProperty(FIELD_NAME_END_TIME) long endTime,
		@JsonProperty(FIELD_NAME_DURATION) long duration,
		@JsonProperty(FIELD_NAME_PARALLELISM) int parallelism,
		@JsonProperty(FIELD_NAME_MAX_PARALLELISM) int maxParallelism,
		@JsonProperty(FIELD_NAME_STATUS) JobStatus status
	) {
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(status);

		this.id = id;
		this.name = name;
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.parallelism = parallelism;
		this.maxParallelism = maxParallelism;
		this.status = status;
	}

	public JobID getId() {
		return id;
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

	public JobStatus getStatus() {
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

		JobSummaryInfo jobSummaryInfo = (JobSummaryInfo) o;
		return Objects.equals(id, jobSummaryInfo.id) &&
			Objects.equals(name, jobSummaryInfo.name) &&
			startTime == jobSummaryInfo.startTime &&
			endTime == jobSummaryInfo.endTime &&
			duration == jobSummaryInfo.duration &&
			parallelism == jobSummaryInfo.parallelism &&
			maxParallelism == jobSummaryInfo.maxParallelism &&
			status == jobSummaryInfo.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, startTime, endTime, duration, parallelism, maxParallelism, status);
	}

	@Override
	public String toString() {
		return "JobSummaryInfo{" +
			"id=" + id +
			", name='" + name + '\'' +
			", startTime=" + startTime +
			", endTime=" + endTime +
			", duration=" + duration +
			", parallelism=" + parallelism +
			", maxParallelism=" + maxParallelism +
			", status=" + status +
			'}';
	}
}
