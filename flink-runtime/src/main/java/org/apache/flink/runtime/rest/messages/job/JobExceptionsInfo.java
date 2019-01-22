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

import org.apache.flink.runtime.rest.handler.job.JobExceptionsHandler;
import org.apache.flink.runtime.rest.messages.ResponseBody;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.Objects;

/**
 * Response type of the {@link JobExceptionsHandler}.
 */
public class JobExceptionsInfo implements ResponseBody {

	private static final String FIELD_NAME_NUM_EXCEPTIONS = "numExceptions";
	private static final String FIELD_NAME_EXCEPTIONS = "exceptions";

	@JsonProperty(FIELD_NAME_NUM_EXCEPTIONS)
	private final int numExceptions;

	@JsonProperty(FIELD_NAME_EXCEPTIONS)
	private final Collection<ExceptionInfo> exceptions;

	@JsonCreator
	public JobExceptionsInfo(
		@JsonProperty(FIELD_NAME_NUM_EXCEPTIONS) int numExceptions,
		@JsonProperty(FIELD_NAME_EXCEPTIONS) Collection<ExceptionInfo> exceptions
	) {
		this.numExceptions = numExceptions;
		this.exceptions = exceptions;
	}

	public int getNumExceptions() {
		return numExceptions;
	}

	public Collection<ExceptionInfo> getExceptions() {
		return exceptions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		JobExceptionsInfo that = (JobExceptionsInfo) o;
		return numExceptions == that.numExceptions &&
			Objects.equals(exceptions, that.exceptions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(numExceptions, exceptions);
	}

	@Override
	public String toString() {
		return "JobExceptionsInfo{" +
			"numExceptions=" + numExceptions +
			", exceptions=" + exceptions +
			'}';
	}
}
