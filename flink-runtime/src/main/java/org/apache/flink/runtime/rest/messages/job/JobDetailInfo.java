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
import org.apache.flink.runtime.rest.messages.json.RawJsonDeserializer;
import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonRawValue;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Collection;
import java.util.Objects;

/**
 * Details about a job.
 */
public class JobDetailInfo implements ResponseBody {

	public static final String FIELD_NAME_ID = "id";

	public static final String FIELD_NAME_NAME = "name";

	public static final String FIELD_NAME_START_TIME = "startTime";

	public static final String FIELD_NAME_END_TIME = "endTime";

	public static final String FIELD_NAME_DURATION = "duration";

	public static final String FIELD_NAME_JOB_STATUS = "status";

	public static final String FIELD_NAME_JOB_VERTICES = "vertices";

	public static final String FIELD_NAME_JSON_PLAN = "plan";

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

	@JsonProperty(FIELD_NAME_JOB_STATUS)
	private final JobStatus status;

	@JsonProperty(FIELD_NAME_JSON_PLAN)
	@JsonRawValue
	private final String jsonPlan;

	@JsonProperty(FIELD_NAME_JOB_VERTICES)
	private final Collection<VertexSummaryInfo> vertices;

	@JsonCreator
	public JobDetailInfo(
		@JsonDeserialize(using = JobIDDeserializer.class) @JsonProperty(FIELD_NAME_ID) JobID id,
		@JsonProperty(FIELD_NAME_NAME) String name,
		@JsonProperty(FIELD_NAME_START_TIME) long startTime,
		@JsonProperty(FIELD_NAME_END_TIME) long endTime,
		@JsonProperty(FIELD_NAME_DURATION) long duration,
		@JsonProperty(FIELD_NAME_JOB_STATUS) JobStatus status,
		@JsonProperty(FIELD_NAME_JSON_PLAN) @JsonDeserialize(using = RawJsonDeserializer.class) String jsonPlan,
		@JsonProperty(FIELD_NAME_JOB_VERTICES) Collection<VertexSummaryInfo> vertices
	) {
		this.id = Preconditions.checkNotNull(id);
		this.name = Preconditions.checkNotNull(name);
		this.status = Preconditions.checkNotNull(status);
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.jsonPlan = Preconditions.checkNotNull(jsonPlan);
		this.vertices = Preconditions.checkNotNull(vertices);
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

	public JobStatus getStatus() {
		return status;
	}

	public String getJsonPlan() {
		return jsonPlan;
	}

	public Collection<VertexSummaryInfo> getVertices() {
		return vertices;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		JobDetailInfo that = (JobDetailInfo) o;
		return id.equals(that.id) &&
			name.equals(that.name) &&
			startTime == that.startTime &&
			endTime == that.endTime &&
			duration == that.duration &&
			status == that.status &&
			vertices.equals(that.vertices) &&
			jsonPlan.equals(that.jsonPlan);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, startTime, endTime, duration, status, vertices, jsonPlan);
	}

	@Override
	public String toString() {
		return "JobDetailInfo{" +
			"id=" + id +
			", name='" + name + '\'' +
			", startTime=" + startTime +
			", endTime=" + endTime +
			", duration=" + duration +
			", status=" + status +
			", jsonPlan='" + jsonPlan + '\'' +
			", vertices=" + vertices +
			'}';
	}
}
