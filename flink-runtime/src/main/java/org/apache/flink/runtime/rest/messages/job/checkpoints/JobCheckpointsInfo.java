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

import org.apache.flink.runtime.rest.handler.job.checkpoints.JobCheckpointsHandler;
import org.apache.flink.runtime.rest.messages.ResponseBody;
import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.Objects;

/**
 * Response of the {@link JobCheckpointsHandler}. This class contains information about
 * the checkpointing of a given job.
 */
public class JobCheckpointsInfo implements ResponseBody {

	private static final String FIELD_NAME_NUM_PENDING_CHECKPOINTS = "numPendingCheckpoints";
	private static final String FIELD_NAME_NUM_COMPLETD_CHECKPOINTS = "numCompletedCheckpoints";
	private static final String FIELD_NAME_NUM_FAILED_CHECKPOINTS = "numFailedCheckpoints";
	private static final String FIELD_NAME_LAST_COMPLETED_CHECKPOINT = "lastCompletedCheckpoint";
	private static final String FIELD_NAME_LAST_FAILED_CHECKPOINT = "lastFailedCheckpoint";
	private static final String FIELD_NAME_CHECKPOINTS = "checkpoints";

	@JsonProperty(FIELD_NAME_NUM_PENDING_CHECKPOINTS)
	private final int numPendingCheckpoints;

	@JsonProperty(FIELD_NAME_NUM_COMPLETD_CHECKPOINTS)
	private final int numCompletedCheckpoints;

	@JsonProperty(FIELD_NAME_NUM_FAILED_CHECKPOINTS)
	private final int numFailedCheckpoints;

	@JsonProperty(FIELD_NAME_LAST_COMPLETED_CHECKPOINT)
	private final CheckpointSummaryInfo lastCompletedCheckpoint;

	@JsonProperty(FIELD_NAME_LAST_FAILED_CHECKPOINT)
	private final CheckpointSummaryInfo lastFailedCheckpoint;

	@JsonProperty(FIELD_NAME_CHECKPOINTS)
	private final Collection<CheckpointSummaryInfo> checkpoints;

	@JsonCreator
	public JobCheckpointsInfo(
		@JsonProperty(FIELD_NAME_NUM_PENDING_CHECKPOINTS) int numPendingCheckpoints,
		@JsonProperty(FIELD_NAME_NUM_COMPLETD_CHECKPOINTS) int numCompletedCheckpoints,
		@JsonProperty(FIELD_NAME_NUM_FAILED_CHECKPOINTS) int numFailedCheckpoints,
		@JsonProperty(FIELD_NAME_LAST_COMPLETED_CHECKPOINT) CheckpointSummaryInfo lastCompletedCheckpoint,
		@JsonProperty(FIELD_NAME_LAST_FAILED_CHECKPOINT) CheckpointSummaryInfo lastFailedCheckpoint,
		@JsonProperty(FIELD_NAME_CHECKPOINTS) Collection<CheckpointSummaryInfo> checkpoints
	) {
		Preconditions.checkNotNull(checkpoints);

		this.numPendingCheckpoints = numPendingCheckpoints;
		this.numCompletedCheckpoints = numCompletedCheckpoints;
		this.numFailedCheckpoints = numFailedCheckpoints;
		this.lastCompletedCheckpoint = lastCompletedCheckpoint;
		this.lastFailedCheckpoint = lastFailedCheckpoint;
		this.checkpoints = checkpoints;
	}

	public int getNumPendingCheckpoints() {
		return numPendingCheckpoints;
	}

	public int getNumCompletedCheckpoints() {
		return numCompletedCheckpoints;
	}

	public int getNumFailedCheckpoints() {
		return numFailedCheckpoints;
	}

	public CheckpointSummaryInfo getLastCompletedCheckpoint() {
		return lastCompletedCheckpoint;
	}

	public CheckpointSummaryInfo getLastFailedCheckpoint() {
		return lastFailedCheckpoint;
	}

	public Collection<CheckpointSummaryInfo> getCheckpoints() {
		return checkpoints;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		JobCheckpointsInfo that = (JobCheckpointsInfo) o;
		return numPendingCheckpoints == that.numPendingCheckpoints &&
			numCompletedCheckpoints == that.numCompletedCheckpoints &&
			numFailedCheckpoints == that.numFailedCheckpoints &&
			Objects.equals(lastCompletedCheckpoint, that.lastCompletedCheckpoint) &&
			Objects.equals(lastFailedCheckpoint, that.lastFailedCheckpoint) &&
			Objects.equals(checkpoints, that.checkpoints);
	}

	@Override
	public int hashCode() {
		return Objects.hash(numPendingCheckpoints, numCompletedCheckpoints, numFailedCheckpoints,
			lastCompletedCheckpoint, lastFailedCheckpoint, checkpoints);
	}

	@Override
	public String toString() {
		return "JobCheckpointsInfo{" +
			", numPendingCheckpoints=" + numPendingCheckpoints +
			", numCompletedCheckpoints=" + numCompletedCheckpoints +
			", numFailedCheckpoints=" + numFailedCheckpoints +
			", lastCompletedCheckpoint=" + lastCompletedCheckpoint +
			", lastFailedCheckpoint=" + lastFailedCheckpoint +
			", checkpoints=" + checkpoints +
			'}';
	}
}
