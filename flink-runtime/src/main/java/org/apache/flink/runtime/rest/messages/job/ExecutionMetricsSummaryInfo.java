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

import org.apache.flink.runtime.rest.messages.MetricSummaryInfo;
import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * The summary information for a collection of {@link ExecutionMetricsInfo}.
 */
public class ExecutionMetricsSummaryInfo {

	private static final String FIELD_NAME_WATERMARK = "watermark";
	private static final String FIELD_NAME_IN_QUEUE_USAGE = "inQueueUsage";
	private static final String FIELD_NAME_OUT_QUEUE_USAGE = "outQueueUsage";
	private static final String FIELD_NAME_RECORDS_IN = "recordsIn";
	private static final String FIELD_NAME_RECORDS_OUT = "recordsOut";
	private static final String FIELD_NAME_RECORDS_IN_RATE = "recordsInRate";
	private static final String FIELD_NAME_RECORDS_OUT_RATE = "recordsOutRate";
	private static final String FIELD_NAME_BYTES_IN = "bytesIn";
	private static final String FIELD_NAME_BYTES_OUT = "bytesOut";
	private static final String FIELD_NAME_BYTES_IN_RATE = "bytesInRate";
	private static final String FIELD_NAME_BYTES_OUT_RATE = "bytesOutRate";

	@JsonProperty(FIELD_NAME_WATERMARK)
	private final MetricSummaryInfo watermarkSummary;

	@JsonProperty(FIELD_NAME_IN_QUEUE_USAGE)
	private final MetricSummaryInfo inQueueUsageSummary;

	@JsonProperty(FIELD_NAME_OUT_QUEUE_USAGE)
	private final MetricSummaryInfo outQueueUsageSummary;

	@JsonProperty(FIELD_NAME_RECORDS_IN)
	private final MetricSummaryInfo numRecordsInSummary;

	@JsonProperty(FIELD_NAME_RECORDS_OUT)
	private final MetricSummaryInfo numRecordsOutSummary;

	@JsonProperty(FIELD_NAME_RECORDS_IN_RATE)
	private final MetricSummaryInfo rateRecordsInSummary;

	@JsonProperty(FIELD_NAME_RECORDS_OUT_RATE)
	private final MetricSummaryInfo rateRecordsOutSummary;

	@JsonProperty(FIELD_NAME_BYTES_IN)
	private final MetricSummaryInfo numBytesInSummary;

	@JsonProperty(FIELD_NAME_BYTES_OUT)
	private final MetricSummaryInfo numBytesOutSummary;

	@JsonProperty(FIELD_NAME_BYTES_IN_RATE)
	private final MetricSummaryInfo rateBytesInSummary;

	@JsonProperty(FIELD_NAME_BYTES_OUT_RATE)
	private final MetricSummaryInfo rateBytesOutSummary;

	@JsonCreator
	public ExecutionMetricsSummaryInfo(
		@JsonProperty(FIELD_NAME_WATERMARK) MetricSummaryInfo watermarkSummary,
		@JsonProperty(FIELD_NAME_IN_QUEUE_USAGE) MetricSummaryInfo inQueueUsageSummary,
		@JsonProperty(FIELD_NAME_OUT_QUEUE_USAGE) MetricSummaryInfo outQueueUsageSummary,
		@JsonProperty(FIELD_NAME_RECORDS_IN) MetricSummaryInfo numRecordsInSummary,
		@JsonProperty(FIELD_NAME_RECORDS_OUT) MetricSummaryInfo numRecordsOutSummary,
		@JsonProperty(FIELD_NAME_RECORDS_IN_RATE) MetricSummaryInfo rateRecordsInSummary,
		@JsonProperty(FIELD_NAME_RECORDS_OUT_RATE) MetricSummaryInfo rateRecordsOutSummary,
		@JsonProperty(FIELD_NAME_BYTES_IN) MetricSummaryInfo numBytesInSummary,
		@JsonProperty(FIELD_NAME_BYTES_OUT) MetricSummaryInfo numBytesOutSummary,
		@JsonProperty(FIELD_NAME_BYTES_IN_RATE) MetricSummaryInfo rateBytesInSummary,
		@JsonProperty(FIELD_NAME_BYTES_OUT_RATE)MetricSummaryInfo rateBytesOutSummary
	) {
		Preconditions.checkNotNull(watermarkSummary);
		Preconditions.checkNotNull(inQueueUsageSummary);
		Preconditions.checkNotNull(outQueueUsageSummary);
		Preconditions.checkNotNull(numRecordsInSummary);
		Preconditions.checkNotNull(numRecordsOutSummary);
		Preconditions.checkNotNull(rateRecordsInSummary);
		Preconditions.checkNotNull(rateRecordsOutSummary);
		Preconditions.checkNotNull(numBytesInSummary);
		Preconditions.checkNotNull(numBytesOutSummary);
		Preconditions.checkNotNull(rateBytesInSummary);
		Preconditions.checkNotNull(rateBytesOutSummary);

		this.watermarkSummary = watermarkSummary;
		this.inQueueUsageSummary = inQueueUsageSummary;
		this.outQueueUsageSummary = outQueueUsageSummary;
		this.numRecordsInSummary = numRecordsInSummary;
		this.numRecordsOutSummary = numRecordsOutSummary;
		this.rateRecordsInSummary = rateRecordsInSummary;
		this.rateRecordsOutSummary = rateRecordsOutSummary;
		this.numBytesInSummary = numBytesInSummary;
		this.numBytesOutSummary = numBytesOutSummary;
		this.rateBytesInSummary = rateBytesInSummary;
		this.rateBytesOutSummary = rateBytesOutSummary;
	}

	public MetricSummaryInfo getWatermarkSummary() {
		return watermarkSummary;
	}

	public MetricSummaryInfo getInQueueUsageSummary() {
		return inQueueUsageSummary;
	}

	public MetricSummaryInfo getOutQueueUsageSummary() {
		return outQueueUsageSummary;
	}

	public MetricSummaryInfo getNumRecordsInSummary() {
		return numRecordsInSummary;
	}

	public MetricSummaryInfo getNumRecordsOutSummary() {
		return numRecordsOutSummary;
	}

	public MetricSummaryInfo getRateRecordsInSummary() {
		return rateRecordsInSummary;
	}

	public MetricSummaryInfo getRateRecordsOutSummary() {
		return rateRecordsOutSummary;
	}

	public MetricSummaryInfo getNumBytesInSummary() {
		return numBytesInSummary;
	}

	public MetricSummaryInfo getNumBytesOutSummary() {
		return numBytesOutSummary;
	}

	public MetricSummaryInfo getRateBytesInSummary() {
		return rateBytesInSummary;
	}

	public MetricSummaryInfo getRateBytesOutSummary() {
		return rateBytesOutSummary;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ExecutionMetricsSummaryInfo that = (ExecutionMetricsSummaryInfo) o;
		return Objects.equals(watermarkSummary, that.watermarkSummary) &&
			Objects.equals(inQueueUsageSummary, that.inQueueUsageSummary) &&
			Objects.equals(outQueueUsageSummary, that.outQueueUsageSummary) &&
			Objects.equals(numRecordsInSummary, that.numRecordsInSummary) &&
			Objects.equals(numRecordsOutSummary, that.numRecordsOutSummary) &&
			Objects.equals(rateRecordsInSummary, that.rateRecordsInSummary) &&
			Objects.equals(rateRecordsOutSummary, that.rateRecordsOutSummary) &&
			Objects.equals(numBytesInSummary, that.numBytesInSummary) &&
			Objects.equals(numBytesOutSummary, that.numBytesOutSummary) &&
			Objects.equals(rateBytesInSummary, that.rateBytesInSummary) &&
			Objects.equals(rateBytesOutSummary, that.rateBytesOutSummary);
	}

	@Override
	public int hashCode() {
		return Objects.hash(watermarkSummary, inQueueUsageSummary, outQueueUsageSummary,
			numRecordsInSummary, numRecordsOutSummary, rateRecordsInSummary, rateRecordsOutSummary,
			numBytesInSummary, numBytesOutSummary, rateBytesInSummary, rateBytesOutSummary);
	}

	@Override
	public String toString() {
		return "ExecutionMetricsSummaryInfo{" +
			"watermarkSummary=" + watermarkSummary +
			", inQueueUsageSummary=" + inQueueUsageSummary +
			", outQueueUsageSummary=" + outQueueUsageSummary +
			", numRecordsInSummary=" + numRecordsInSummary +
			", numRecordsOutSummary=" + numRecordsOutSummary +
			", rateRecordsInSummary=" + rateRecordsInSummary +
			", rateRecordsOutSummary=" + rateRecordsOutSummary +
			", numBytesInSummary=" + numBytesInSummary +
			", numBytesOutSummary=" + numBytesOutSummary +
			", rateBytesInSummary=" + rateBytesInSummary +
			", rateBytesOutSummary=" + rateBytesOutSummary +
			'}';
	}
}
