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

import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.rest.messages.ResponseBody;
import org.apache.flink.runtime.rest.messages.json.JobVertexIDDeserializer;
import org.apache.flink.runtime.rest.messages.json.JobVertexIDSerializer;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Collection;
import java.util.Objects;

/**
 * The detailed information for a vertex.
 */
public class VertexDetailInfo implements ResponseBody {

	private static final String FIELD_NAME_ID = "id";
	private static final String FIELD_NAME_NAME = "name";
	private static final String FIELD_NAME_PARALLELISM = "parallelism";
	private static final String FIELD_NAME_MAX_PARALLELISM = "maxParallelism";
	private static final String FIELD_NAME_TASKS = "tasks";

	@JsonProperty(FIELD_NAME_ID)
	@JsonSerialize(using = JobVertexIDSerializer.class)
	private final JobVertexID id;

	@JsonProperty(FIELD_NAME_NAME)
	private final String name;

	@JsonProperty(FIELD_NAME_PARALLELISM)
	private final int parallelism;

	@JsonProperty(FIELD_NAME_MAX_PARALLELISM)
	private final int maxParallelism;

	@JsonProperty(FIELD_NAME_TASKS)
	private final Collection<TaskSummaryInfo> tasks;

	@JsonCreator
	public VertexDetailInfo(
		@JsonProperty(FIELD_NAME_ID) @JsonDeserialize(using = JobVertexIDDeserializer.class) JobVertexID id,
		@JsonProperty(FIELD_NAME_NAME) String name,
		@JsonProperty(FIELD_NAME_PARALLELISM) int parallelism,
		@JsonProperty(FIELD_NAME_MAX_PARALLELISM) int maxParallelism,
		@JsonProperty(FIELD_NAME_TASKS) Collection<TaskSummaryInfo> tasks
	) {
		this.id = id;
		this.name = name;
		this.parallelism = parallelism;
		this.maxParallelism = maxParallelism;
		this.tasks = tasks;
	}

	public JobVertexID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getParallelism() {
		return parallelism;
	}

	public int getMaxParallelism() {
		return maxParallelism;
	}

	public Collection<TaskSummaryInfo> getTasks() {
		return tasks;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		VertexDetailInfo that = (VertexDetailInfo) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(name, that.name) &&
			Objects.equals(parallelism, that.parallelism) &&
			Objects.equals(maxParallelism, that.maxParallelism) &&
			Objects.equals(tasks, that.tasks);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, parallelism, maxParallelism, tasks);
	}

	@Override
	public String toString() {
		return "VertexDetailInfo{" +
			"id=" + id +
			", name='" + name + '\'' +
			", parallelism=" + parallelism +
			", maxParallelism=" + maxParallelism +
			", tasks=" + tasks +
			'}';
	}
}
