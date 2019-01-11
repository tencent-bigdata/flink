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

import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * The summary information for a task.
 */
public class TaskSummaryInfo {

	private static final String FIELD_NAME_INDEX = "index";
	private static final String FIELD_NAME_ATTEMPTS = "attempts";
	private static final String FIELD_NAME_CURRENT_EXECUTION = "currentExecution";

	@JsonProperty(FIELD_NAME_INDEX)
	private final int index;

	@JsonProperty(FIELD_NAME_ATTEMPTS)
	private final int numAttempts;

	@JsonProperty(FIELD_NAME_CURRENT_EXECUTION)
	private final ExecutionInfo currentExecution;

	@JsonCreator
	public TaskSummaryInfo(
		@JsonProperty(FIELD_NAME_INDEX) int index,
		@JsonProperty(FIELD_NAME_ATTEMPTS) int numAttempts,
		@JsonProperty(FIELD_NAME_CURRENT_EXECUTION) ExecutionInfo currentExecution
	) {
		Preconditions.checkNotNull(currentExecution);

		this.index = index;
		this.numAttempts = numAttempts;
		this.currentExecution = currentExecution;
	}

	public int getIndex() {
		return index;
	}

	public int getNumAttempts() {
		return numAttempts;
	}

	public ExecutionInfo getCurrentExecution() {
		return currentExecution;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TaskSummaryInfo that = (TaskSummaryInfo) o;
		return index == that.index &&
			numAttempts == that.numAttempts &&
			Objects.equals(currentExecution, that.currentExecution);
	}

	@Override
	public int hashCode() {
		return Objects.hash(index, numAttempts, currentExecution);
	}

	@Override
	public String toString() {
		return "TaskSummaryInfo{" +
			"index=" + index +
			", numAttempts=" + numAttempts +
			", currentExecution=" + currentExecution +
			'}';
	}
}
