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

package org.apache.flink.runtime.rest.messages;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * This class contains the summary for a collection of metrics.
 */
public class MetricSummaryInfo {

	private static final String FIELD_NAME_MAX = "max";
	private static final String FIELD_NAME_MIN = "min";
	private static final String FIELD_NAME_SUM = "sum";
	private static final String FIELD_NAME_AVG = "avg";

	@JsonProperty(FIELD_NAME_MAX)
	private final double max;

	@JsonProperty(FIELD_NAME_MIN)
	private final double min;

	@JsonProperty(FIELD_NAME_SUM)
	private final double sum;

	@JsonProperty(FIELD_NAME_AVG)
	private final double avg;

	@JsonCreator
	public MetricSummaryInfo(
		@JsonProperty(FIELD_NAME_MAX) double max,
		@JsonProperty(FIELD_NAME_MIN) double min,
		@JsonProperty(FIELD_NAME_SUM) double sum,
		@JsonProperty(FIELD_NAME_AVG) double avg
	) {
		this.max = max;
		this.min = min;
		this.sum = sum;
		this.avg = avg;
	}

	public double getMax() {
		return max;
	}

	public double getMin() {
		return min;
	}

	public double getSum() {
		return sum;
	}

	public double getAvg() {
		return avg;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MetricSummaryInfo that = (MetricSummaryInfo) o;
		return Double.compare(that.max, max) == 0 &&
			Double.compare(that.min, min) == 0 &&
			Double.compare(that.sum, sum) == 0 &&
			Double.compare(that.avg, avg) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(max, min, sum, avg);
	}

	@Override
	public String toString() {
		return "MetricSummaryInfo{" +
			"max=" + max +
			", min=" + min +
			", sum=" + sum +
			", avg=" + avg +
			'}';
	}
}
