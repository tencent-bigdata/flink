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

import org.apache.flink.runtime.rest.messages.MetricSummaryBuilder;

/**
 * A helper class to create {@link CheckpointMetricsSummaryInfo}.
 */
public class CheckpointMetricsSummaryBuilder {

	private final MetricSummaryBuilder alignDurationSummaryBuilder;

	private final MetricSummaryBuilder syncDurationSummaryBuilder;

	private final MetricSummaryBuilder asyncDurationSummaryBuilder;

	private final MetricSummaryBuilder durationSummaryBuilder;

	private final MetricSummaryBuilder sizeSummaryBuilder;

	public CheckpointMetricsSummaryBuilder() {
		this.alignDurationSummaryBuilder = new MetricSummaryBuilder();
		this.syncDurationSummaryBuilder = new MetricSummaryBuilder();
		this.asyncDurationSummaryBuilder = new MetricSummaryBuilder();
		this.durationSummaryBuilder = new MetricSummaryBuilder();
		this.sizeSummaryBuilder = new MetricSummaryBuilder();
	}

	public void add(CheckpointMetricsInfo metrics) {
		alignDurationSummaryBuilder.add(metrics.getAlignDuration());
		syncDurationSummaryBuilder.add(metrics.getSyncDuration());
		asyncDurationSummaryBuilder.add(metrics.getAsyncDuration());
		durationSummaryBuilder.add(metrics.getDuration());
		sizeSummaryBuilder.add(metrics.getSize());
	}

	public CheckpointMetricsSummaryInfo build() {
		return new CheckpointMetricsSummaryInfo(
			alignDurationSummaryBuilder.build(),
			syncDurationSummaryBuilder.build(),
			asyncDurationSummaryBuilder.build(),
			durationSummaryBuilder.build(),
			sizeSummaryBuilder.build()
		);
	}
}
