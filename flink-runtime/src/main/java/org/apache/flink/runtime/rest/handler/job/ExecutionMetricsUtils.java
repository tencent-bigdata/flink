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

package org.apache.flink.runtime.rest.handler.job;

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.executiongraph.AccessExecution;
import org.apache.flink.runtime.executiongraph.IOMetrics;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.metrics.MetricNames;
import org.apache.flink.runtime.rest.handler.legacy.metrics.MetricFetcher;
import org.apache.flink.runtime.rest.handler.legacy.metrics.MetricStore;
import org.apache.flink.runtime.rest.messages.job.ExecutionMetricsInfo;

import javax.annotation.Nullable;

/**
 * A helper class to construct {@link ExecutionMetricsInfo} for an execution.
 */
public class ExecutionMetricsUtils {

	public static ExecutionMetricsInfo getMetrics(
		JobID jobID,
		JobVertexID vertexID,
		AccessExecution execution,
		@Nullable MetricFetcher fetcher
	) {
		long watermark = -1;
		float inPoolUsage = -1;
		float outPoolUsage = -1;
		long numRecordsIn = -1;
		long numRecordsOut = -1;
		double rateRecordsIn = -1;
		double rateRecordsOut = -1;
		long numBytesIn = -1;
		long numBytesOut = -1;
		double rateBytesIn = -1;
		double rateBytesOut = -1;

		if (execution.getState().isTerminal()) {
			IOMetrics ioMetrics = execution.getIOMetrics();
			if (ioMetrics != null) {
				inPoolUsage = 0;
				outPoolUsage = 0;
				numRecordsIn = ioMetrics.getNumRecordsIn();
				numRecordsOut = ioMetrics.getNumRecordsOut();
				rateRecordsIn = 0;
				rateRecordsOut = 0;
				numBytesIn = ioMetrics.getNumBytesIn();
				numBytesOut = ioMetrics.getNumBytesOut();
				rateBytesIn = 0;
				rateBytesOut = 0;
			}
		} else { // execAttempt is still running, use MetricQueryService instead
			if (fetcher != null) {

				fetcher.update();

				MetricStore.ComponentMetricStore metrics =
					fetcher.getMetricStore().getSubtaskMetricStore(jobID.toString(), vertexID.toString(), execution.getParallelSubtaskIndex());

				if (metrics != null) {

					String watermarkStr = metrics.getMetric(MetricNames.IO_CURRENT_INPUT_WATERMARK);
					if (watermarkStr != null) {
						watermark = Long.parseLong(watermarkStr);
					}

					String inPoolUsageStr = metrics.getMetric(MetricNames.IO_IN_POOL_USAGE);
					if (inPoolUsageStr != null) {
						inPoolUsage = Float.parseFloat(inPoolUsageStr);
					}

					String outPoolUsageStr = metrics.getMetric(MetricNames.IO_OUT_POOL_USAGE);
					if (outPoolUsageStr != null) {
						outPoolUsage = Float.parseFloat(outPoolUsageStr);
					}

					String numRecordsInStr = metrics.getMetric(MetricNames.IO_NUM_RECORDS_IN);
					if (numRecordsInStr != null){
						numRecordsIn = Long.parseLong(numRecordsInStr);
					}

					String numRecordsOutStr = metrics.getMetric(MetricNames.IO_NUM_RECORDS_OUT);
					if (numRecordsOutStr != null) {
						numRecordsOut = Long.parseLong(numRecordsOutStr);
					}

					String rateRecordsInStr = metrics.getMetric(MetricNames.IO_NUM_RECORDS_IN_RATE);
					if (rateRecordsInStr != null) {
						rateRecordsIn = Double.parseDouble(rateRecordsInStr);
					}

					String rateRecordsOutStr = metrics.getMetric(MetricNames.IO_NUM_RECORDS_OUT_RATE);
					if (rateRecordsOutStr != null) {
						rateRecordsOut = Double.parseDouble(rateRecordsOutStr);
					}

					String numBytesInLocalStr = metrics.getMetric(MetricNames.IO_NUM_BYTES_IN_LOCAL);
					String numBytesInRemoteStr = metrics.getMetric(MetricNames.IO_NUM_BYTES_IN_REMOTE);
					if (numBytesInLocalStr != null || numBytesInRemoteStr != null) {
						numBytesIn = 0;

						if (numBytesInLocalStr != null) {
							numBytesIn += Double.parseDouble(numBytesInLocalStr);
						}

						if (numBytesInRemoteStr != null) {
							numBytesIn += Double.parseDouble(numBytesInRemoteStr);
						}
					}

					String numBytesOutStr = metrics.getMetric(MetricNames.IO_NUM_BYTES_OUT);
					if (numBytesOutStr != null) {
						numBytesOut = Long.parseLong(numBytesOutStr);
					}

					String rateBytesInLocalStr = metrics.getMetric(MetricNames.IO_NUM_BYTES_IN_LOCAL_RATE);
					String rateBytesInRemoteStr = metrics.getMetric(MetricNames.IO_NUM_BYTES_IN_REMOTE_RATE);
					if (rateBytesInLocalStr != null || rateBytesInRemoteStr != null) {
						rateBytesIn = 0;

						if (rateBytesInLocalStr != null) {
							rateBytesIn += Double.parseDouble(rateBytesInLocalStr);
						}

						if (rateBytesInRemoteStr != null) {
							rateBytesIn += Double.parseDouble(rateBytesInRemoteStr);
						}
					}

					String rateBytesOutStr = metrics.getMetric(MetricNames.IO_NUM_BYTES_OUT_RATE);
					if (rateBytesOutStr != null) {
						rateBytesOut = Double.parseDouble(rateBytesOutStr);
					}
				}
			}
		}

		return new ExecutionMetricsInfo(
			watermark,
			inPoolUsage,
			outPoolUsage,
			numRecordsIn,
			numRecordsOut,
			rateRecordsIn,
			rateRecordsOut,
			numBytesIn,
			numBytesOut,
			rateBytesIn,
			rateBytesOut
		);
	}
}
