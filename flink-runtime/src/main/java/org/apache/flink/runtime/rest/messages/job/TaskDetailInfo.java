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

import org.apache.flink.runtime.rest.messages.ResponseBody;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.Objects;

/**
 * The detailed information for a task.
 */
public class TaskDetailInfo implements ResponseBody {

	private static final String FIELD_NAME_INDEX = "index";
	private static final String FIELD_NAME_ATTEMPTS = "numAttempts";
	private static final String FIELD_NAME_EXECUTIONS = "executions";

	@JsonProperty(FIELD_NAME_INDEX)
	private final int index;

	@JsonProperty(FIELD_NAME_ATTEMPTS)
	private final int numAttempts;

	@JsonProperty(FIELD_NAME_EXECUTIONS)
	private final Collection<ExecutionInfo> executions;

	@JsonCreator
	public TaskDetailInfo(
		@JsonProperty(FIELD_NAME_INDEX) int index,
		@JsonProperty(FIELD_NAME_ATTEMPTS) int numAttempts,
		@JsonProperty(FIELD_NAME_EXECUTIONS) Collection<ExecutionInfo> executions
	) {
		this.index = index;
		this.numAttempts = numAttempts;
		this.executions = executions;
	}

	public int getIndex() {
		return index;
	}

	public int getNumAttempts() {
		return numAttempts;
	}

	public Collection<ExecutionInfo> getExecutions() {
		return executions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TaskDetailInfo that = (TaskDetailInfo) o;
		return index == that.index &&
			numAttempts == that.numAttempts &&
			Objects.equals(executions, that.executions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(index, numAttempts, executions);
	}

	@Override
	public String toString() {
		return "TaskDetailInfo{" +
			"index=" + index +
			", numAttempts=" + numAttempts +
			", executions=" + executions +
			'}';
	}
}
