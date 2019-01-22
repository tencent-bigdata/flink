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

package org.apache.flink.runtime.executiongraph;

import org.apache.flink.runtime.taskmanager.TaskManagerLocation;
import org.apache.flink.util.ExceptionUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * The trace for the failures of the job.
 */
public class ExceptionTrace implements Serializable {

	private static final long serialVersionUID = 1L;

	private final long failInstant;

	private final String failCause;

	private final ExecutionAttemptID executionId;

	// TODO:: remove following fields once we allow the retrieval of executions from execution history

	private final String taskName;

	private final int attemptNumber;

	private final TaskManagerLocation taskManagerLocation;

	public ExceptionTrace(
		long failInstant,
		Throwable failCause,
		ExecutionAttemptID executionId,
		String taskName,
		int attemptNumber,
		TaskManagerLocation taskManagerLocation
	) {
		this.failInstant = failInstant;
		this.failCause = ExceptionUtils.stringifyException(failCause);
		this.executionId = executionId;
		this.taskName = taskName;
		this.attemptNumber = attemptNumber;
		this.taskManagerLocation = taskManagerLocation;
	}

	public long getFailInstant() {
		return failInstant;
	}

	public String getFailCause() {
		return failCause;
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

	public TaskManagerLocation getTaskManagerLocation() {
		return taskManagerLocation;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ExceptionTrace that = (ExceptionTrace) o;
		return failInstant == that.failInstant &&
			Objects.equals(failCause, that.failCause) &&
			Objects.equals(executionId, that.executionId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(failInstant, failCause, executionId);
	}

	@Override
	public String toString() {
		return "ExceptionTrace{" +
			"failInstant=" + failInstant +
			", failCause='" + failCause + '\'' +
			", executionId=" + executionId +
			", taskName=" + taskName +
			", attemptNumber=" + attemptNumber +
			", taskManagerLocation=" + taskManagerLocation +
			'}';
	}
}
