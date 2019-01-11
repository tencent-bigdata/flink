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

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * The information for an execution's metrics.
 */
public final class ExecutionMetricsInfo implements Serializable {

	private static final long serialVersionUID = 7509907688832811771L;

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
	private final long watermark;

	@JsonProperty(FIELD_NAME_IN_QUEUE_USAGE)
	private final float inQueueUsage;

	@JsonProperty(FIELD_NAME_OUT_QUEUE_USAGE)
	private final float outQueueUsage;

	@JsonProperty(FIELD_NAME_RECORDS_IN)
	private final long numRecordsIn;

	@JsonProperty(FIELD_NAME_RECORDS_OUT)
	private final long numRecordsOut;

	@JsonProperty(FIELD_NAME_RECORDS_IN_RATE)
	private final double rateRecordsIn;

	@JsonProperty(FIELD_NAME_RECORDS_OUT_RATE)
	private final double rateRecordsOut;

	@JsonProperty(FIELD_NAME_BYTES_IN)
	private final long numBytesIn;

	@JsonProperty(FIELD_NAME_BYTES_OUT)
	private final long numBytesOut;

	@JsonProperty(FIELD_NAME_BYTES_IN_RATE)
	private final double rateBytesIn;

	@JsonProperty(FIELD_NAME_BYTES_OUT_RATE)
	private final double rateBytesOut;

	@JsonCreator
	public ExecutionMetricsInfo(
		@JsonProperty(FIELD_NAME_WATERMARK) long watermark,
		@JsonProperty(FIELD_NAME_IN_QUEUE_USAGE) float inQueueUsage,
		@JsonProperty(FIELD_NAME_OUT_QUEUE_USAGE) float outQueueUsage,
		@JsonProperty(FIELD_NAME_RECORDS_IN) long numRecordsIn,
		@JsonProperty(FIELD_NAME_RECORDS_OUT) long numRecordsOut,
		@JsonProperty(FIELD_NAME_RECORDS_IN_RATE) double rateRecordsIn,
		@JsonProperty(FIELD_NAME_RECORDS_OUT_RATE) double rateRecordsOut,
		@JsonProperty(FIELD_NAME_BYTES_IN) long numBytesIn,
		@JsonProperty(FIELD_NAME_BYTES_OUT) long numBytesOut,
		@JsonProperty(FIELD_NAME_BYTES_IN_RATE) double rateBytesIn,
		@JsonProperty(FIELD_NAME_BYTES_OUT_RATE) double rateBytesOut
	) {
		this.watermark = watermark;
		this.inQueueUsage = inQueueUsage;
		this.outQueueUsage = outQueueUsage;
		this.numRecordsIn = numRecordsIn;
		this.numRecordsOut = numRecordsOut;
		this.rateRecordsIn = rateRecordsIn;
		this.rateRecordsOut = rateRecordsOut;
		this.numBytesIn = numBytesIn;
		this.numBytesOut = numBytesOut;
		this.rateBytesIn = rateBytesIn;
		this.rateBytesOut = rateBytesOut;
	}

	public long getWatermark() {
		return watermark;
	}

	public float getInQueueUsage() {
		return inQueueUsage;
	}

	public float getOutQueueUsage() {
		return outQueueUsage;
	}

	public long getNumRecordsIn() {
		return numRecordsIn;
	}

	public long getNumRecordsOut() {
		return numRecordsOut;
	}

	public double getRateRecordsIn() {
		return rateRecordsIn;
	}

	public double getRateRecordsOut() {
		return rateRecordsOut;
	}

	public long getNumBytesIn() {
		return numBytesIn;
	}

	public long getNumBytesOut() {
		return numBytesOut;
	}

	public double getRateBytesIn() {
		return rateBytesIn;
	}

	public double getRateBytesOut() {
		return rateBytesOut;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ExecutionMetricsInfo that = (ExecutionMetricsInfo) o;
		return watermark == that.watermark &&
			Float.compare(that.inQueueUsage, inQueueUsage) == 0 &&
			Float.compare(that.outQueueUsage, outQueueUsage) == 0 &&
			numRecordsIn == that.numRecordsIn &&
			numRecordsOut == that.numRecordsOut &&
			Double.compare(rateRecordsIn, that.rateRecordsIn) == 0 &&
			Double.compare(rateRecordsOut, that.rateRecordsOut) == 0 &&
			numBytesIn == that.numBytesIn &&
			numBytesOut == that.numBytesOut &&
			Double.compare(rateBytesIn, that.rateBytesIn) == 0 &&
			Double.compare(rateBytesOut, that.rateBytesOut) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(watermark, inQueueUsage, outQueueUsage, numRecordsIn, numRecordsOut,
			rateRecordsIn, rateRecordsOut, numBytesIn, numBytesOut, rateBytesIn, rateBytesOut);
	}

	@Override
	public String toString() {
		return "ExecutionMetricsInfo{" +
			"watermark=" + watermark +
			", inQueueUsage=" + inQueueUsage +
			", outQueueUsage=" + outQueueUsage +
			", numRecordsIn=" + numRecordsIn +
			", numRecordsOut=" + numRecordsOut +
			", rateRecordsIn=" + rateRecordsIn +
			", rateRecordsOut=" + rateRecordsOut +
			", numBytesIn=" + numBytesIn +
			", numBytesOut=" + numBytesOut +
			", rateBytesIn=" + rateBytesIn +
			", rateBytesOut=" + rateBytesOut +
			'}';
	}
}
