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

/**
 * A helper class to build {@link MetricSummaryInfo}.
 */
public class MetricSummaryBuilder {

	private double max;

	private double min;

	private double sum;

	private int cnt;

	public MetricSummaryBuilder() {
		this.max = 0;
		this.min = Double.MAX_VALUE;
		this.sum = 0;
		this.cnt = 0;
	}

	public void add(double value) {
		if (value < 0) {
			return;
		}

		max = Math.max(max, value);
		min = Math.min(min, value);
		sum += value;
		cnt++;
	}

	public MetricSummaryInfo build() {
		if (cnt == 0) {
			return new MetricSummaryInfo(-1, -1, -1, -1);
		} else {
			return new MetricSummaryInfo(max, min, sum, sum / cnt);
		}
	}
}
