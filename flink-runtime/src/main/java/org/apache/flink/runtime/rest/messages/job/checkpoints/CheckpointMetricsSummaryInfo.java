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

import org.apache.flink.runtime.rest.messages.MetricSummaryInfo;
import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * The summary information for checkpoint metrics.
 */
public class CheckpointMetricsSummaryInfo implements Serializable {

	private static final String FIELD_NAME_ALIGN_DURATION = "alignDuration";
	private static final String FIELD_NAME_SYNC_DURATION = "syncDuration";
	private static final String FIELD_NAME_ASYNC_DURATION = "asyncDuration";
	private static final String FIELD_NAME_DURATION = "duration";
	private static final String FIELD_NAME_SIZE = "size";

	@JsonProperty(FIELD_NAME_ALIGN_DURATION)
	private final MetricSummaryInfo alignDurationSummary;

	@JsonProperty(FIELD_NAME_SYNC_DURATION)
	private final MetricSummaryInfo syncDurationSummary;

	@JsonProperty(FIELD_NAME_ASYNC_DURATION)
	private final MetricSummaryInfo asyncDurationSummary;

	@JsonProperty(FIELD_NAME_DURATION)
	private final MetricSummaryInfo durationSummary;

	@JsonProperty(FIELD_NAME_SIZE)
	private final MetricSummaryInfo sizeSummary;

	@JsonCreator
	public CheckpointMetricsSummaryInfo(
		@JsonProperty(FIELD_NAME_ALIGN_DURATION) MetricSummaryInfo alignDurationSummary,
		@JsonProperty(FIELD_NAME_SYNC_DURATION) MetricSummaryInfo syncDurationSummary,
		@JsonProperty(FIELD_NAME_ASYNC_DURATION) MetricSummaryInfo asyncDurationSummary,
		@JsonProperty(FIELD_NAME_DURATION) MetricSummaryInfo durationSummary,
		@JsonProperty(FIELD_NAME_SIZE) MetricSummaryInfo sizeSummary
	) {
		Preconditions.checkNotNull(alignDurationSummary);
		Preconditions.checkNotNull(syncDurationSummary);
		Preconditions.checkNotNull(asyncDurationSummary);
		Preconditions.checkNotNull(durationSummary);
		Preconditions.checkNotNull(sizeSummary);

		this.alignDurationSummary = alignDurationSummary;
		this.syncDurationSummary = syncDurationSummary;
		this.asyncDurationSummary = asyncDurationSummary;
		this.durationSummary = durationSummary;
		this.sizeSummary = sizeSummary;
	}

	public MetricSummaryInfo getAlignDurationSummary() {
		return alignDurationSummary;
	}

	public MetricSummaryInfo getSyncDurationSummary() {
		return syncDurationSummary;
	}

	public MetricSummaryInfo getAsyncDurationSummary() {
		return asyncDurationSummary;
	}

	public MetricSummaryInfo getDurationSummary() {
		return durationSummary;
	}

	public MetricSummaryInfo getSizeSummary() {
		return sizeSummary;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CheckpointMetricsSummaryInfo that = (CheckpointMetricsSummaryInfo) o;
		return Objects.equals(alignDurationSummary, that.alignDurationSummary) &&
			Objects.equals(syncDurationSummary, that.syncDurationSummary) &&
			Objects.equals(asyncDurationSummary, that.asyncDurationSummary) &&
			Objects.equals(durationSummary, that.durationSummary) &&
			Objects.equals(sizeSummary, that.sizeSummary);
	}

	@Override
	public int hashCode() {
		return Objects.hash(alignDurationSummary, syncDurationSummary, asyncDurationSummary,
			durationSummary, sizeSummary);
	}

	@Override
	public String toString() {
		return "CheckpointMetricsSummaryInfo{" +
			"alignDurationSummary=" + alignDurationSummary +
			", syncDurationSummary=" + syncDurationSummary +
			", asyncDurationSummary=" + asyncDurationSummary +
			", durationSummary=" + durationSummary +
			", sizeSummary=" + sizeSummary +
			'}';
	}
}
