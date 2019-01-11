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

import org.apache.flink.runtime.rest.messages.MetricSummaryBuilder;

/**
 * A helper class to construct {@link ExecutionMetricsSummaryInfo}.
 */
public class ExecutionMetricsSummaryBuilder {

	private final MetricSummaryBuilder watermarkSummary;

	private final MetricSummaryBuilder inQueueUsageSummary;

	private final MetricSummaryBuilder outQueueUsageSummary;

	private final MetricSummaryBuilder numRecordsInSummary;

	private final MetricSummaryBuilder numRecordsOutSummary;

	private final MetricSummaryBuilder rateRecordsInSummary;

	private final MetricSummaryBuilder rateRecordsOutSummary;

	private final MetricSummaryBuilder numBytesInSummary;

	private final MetricSummaryBuilder numBytesOutSummary;

	private final MetricSummaryBuilder rateBytesInSummary;

	private final MetricSummaryBuilder rateBytesOutSummary;

	public ExecutionMetricsSummaryBuilder() {
		this.watermarkSummary = new MetricSummaryBuilder();
		this.inQueueUsageSummary = new MetricSummaryBuilder();
		this.outQueueUsageSummary = new MetricSummaryBuilder();
		this.numRecordsInSummary = new MetricSummaryBuilder();
		this.numRecordsOutSummary = new MetricSummaryBuilder();
		this.rateRecordsInSummary = new MetricSummaryBuilder();
		this.rateRecordsOutSummary = new MetricSummaryBuilder();
		this.numBytesInSummary = new MetricSummaryBuilder();
		this.numBytesOutSummary = new MetricSummaryBuilder();
		this.rateBytesInSummary = new MetricSummaryBuilder();
		this.rateBytesOutSummary = new MetricSummaryBuilder();
	}

	public void add(ExecutionMetricsInfo metricsInfo) {
		watermarkSummary.add(metricsInfo.getWatermark());
		inQueueUsageSummary.add(metricsInfo.getInQueueUsage());
		outQueueUsageSummary.add(metricsInfo.getOutQueueUsage());
		numRecordsInSummary.add(metricsInfo.getNumRecordsIn());
		numRecordsOutSummary.add(metricsInfo.getNumRecordsOut());
		rateRecordsInSummary.add(metricsInfo.getRateRecordsIn());
		rateRecordsOutSummary.add(metricsInfo.getRateRecordsOut());
		numBytesInSummary.add(metricsInfo.getNumBytesIn());
		numBytesOutSummary.add(metricsInfo.getNumBytesOut());
		rateBytesInSummary.add(metricsInfo.getRateBytesIn());
		rateBytesOutSummary.add(metricsInfo.getRateBytesOut());
	}

	public ExecutionMetricsSummaryInfo build() {
		return new ExecutionMetricsSummaryInfo(
			watermarkSummary.build(),
			inQueueUsageSummary.build(),
			outQueueUsageSummary.build(),
			numRecordsInSummary.build(),
			numRecordsOutSummary.build(),
			rateRecordsInSummary.build(),
			rateRecordsOutSummary.build(),
			numBytesInSummary.build(),
			numBytesOutSummary.build(),
			rateBytesInSummary.build(),
			rateBytesOutSummary.build()
		);
	}
}
