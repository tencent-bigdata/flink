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
import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The information for the jobs in the cluster.
 */
public class JobsOverviewInfo implements ResponseBody, Serializable {

	private static final long serialVersionUID = 1L;

	private static final String FIELD_NAME_JOBS_RUNNING = "numRunningJobs";
	private static final String FIELD_NAME_JOBS_FINISHED = "numFinishedJobs";
	private static final String FIELD_NAME_JOBS_CANCELED = "numCanceledJobs";
	private static final String FIELD_NAME_JOBS_FAILED = "numFailedJobs";
	private static final String FIELD_NAME_JOBS = "jobs";

	@JsonProperty(FIELD_NAME_JOBS_RUNNING)
	private final int numJobsRunning;

	@JsonProperty(FIELD_NAME_JOBS_FINISHED)
	private final int numJobsFinished;

	@JsonProperty(FIELD_NAME_JOBS_CANCELED)
	private final int numJobsCanceled;

	@JsonProperty(FIELD_NAME_JOBS_FAILED)
	private final int numJobsFailed;

	@JsonProperty(FIELD_NAME_JOBS)
	private final Collection<JobSummaryInfo> jobs;

	@JsonCreator
	public JobsOverviewInfo(
		@JsonProperty(FIELD_NAME_JOBS_RUNNING) int numJobsRunning,
		@JsonProperty(FIELD_NAME_JOBS_FINISHED) int numJobsFinished,
		@JsonProperty(FIELD_NAME_JOBS_CANCELED) int numJobsCanceled,
		@JsonProperty(FIELD_NAME_JOBS_FAILED) int numJobsFailed,
		@JsonProperty(FIELD_NAME_JOBS) Collection<JobSummaryInfo> jobs
	) {
		Preconditions.checkNotNull(jobs);
		this.numJobsRunning = numJobsRunning;
		this.numJobsFinished = numJobsFinished;
		this.numJobsCanceled = numJobsCanceled;
		this.numJobsFailed = numJobsFailed;
		this.jobs = jobs;
	}

	public static JobsOverviewInfo create(
		Collection<JobSummaryInfo> jobSummaries
	) {
		Preconditions.checkNotNull(jobSummaries);

		int numJobsRunning = 0;
		int numJobsFinished = 0;
		int numJobsCanceled = 0;
		int numJobsFailed = 0;

		for (JobSummaryInfo jobSummary : jobSummaries) {
			switch (jobSummary.getStatus()) {
				case FINISHED:
					numJobsFinished++;
					break;
				case CANCELED:
					numJobsCanceled++;
					break;
				case FAILED:
					numJobsFailed++;
					break;
				default:
					numJobsRunning++;
			}
		}

		return new JobsOverviewInfo(numJobsRunning, numJobsFinished, numJobsCanceled, numJobsFailed, jobSummaries);
	}

	public static JobsOverviewInfo combine(
		JobsOverviewInfo left,
		JobsOverviewInfo right
	) {
		return new JobsOverviewInfo(
			left.getNumJobsRunning() + right.getNumJobsRunning(),
			left.getNumJobsFinished() + right.getNumJobsFinished(),
			left.getNumJobsCanceled() + right.getNumJobsCanceled(),
			left.getNumJobsFailed() + right.getNumJobsFailed(),
			Stream.concat(left.jobs.stream(), right.jobs.stream()).collect(Collectors.toList())
		);
	}

	public int getNumJobsRunning() {
		return numJobsRunning;
	}

	public int getNumJobsFinished() {
		return numJobsFinished;
	}

	public int getNumJobsCanceled() {
		return numJobsCanceled;
	}

	public int getNumJobsFailed() {
		return numJobsFailed;
	}

	public Collection<JobSummaryInfo> getJobs() {
		return jobs;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		JobsOverviewInfo that = (JobsOverviewInfo) o;
		return numJobsRunning == that.numJobsRunning &&
			numJobsFinished == that.numJobsFinished &&
			numJobsCanceled == that.numJobsCanceled &&
			numJobsFailed == that.numJobsFailed &&
			Objects.equals(jobs, that.jobs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(numJobsRunning, numJobsFinished, numJobsCanceled, numJobsFailed, jobs);
	}

	@Override
	public String toString() {
		return "JobsOverviewInfo{" +
			"numJobsRunning=" + numJobsRunning +
			", numJobsFinished=" + numJobsFinished +
			", numJobsCanceled=" + numJobsCanceled +
			", numJobsFailed=" + numJobsFailed +
			", jobs=" + jobs +
			'}';
	}
}
