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

import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.execution.ExecutionState;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;

import java.util.Random;

import static org.apache.flink.runtime.rest.messages.MessageTestUtils.createRandomMetricSummaryInfo;

/**
 * A helper class to create testing messages.
 */
public class JobMessageTestUtils {

	static ExecutionInfo createRandomExecutionInfo(Random random) {

		return new ExecutionInfo(
			new ExecutionAttemptID(),
			random.nextInt(),
			random.nextLong(),
			random.nextLong(),
			random.nextLong(),
			new ExecutorInfo(new ResourceID(Long.toHexString(random.nextLong())), "local" + random.nextInt(), random.nextInt()),
			"failure" + random.nextInt(),
			ExecutionState.values()[random.nextInt(ExecutionState.values().length)],
			createRandomExecutionMetricsInfo(random)
		);
	}

	static ExecutionMetricsInfo createRandomExecutionMetricsInfo(Random random) {
		return new ExecutionMetricsInfo(
			random.nextLong(),
			random.nextFloat(),
			random.nextFloat(),
			random.nextLong(),
			random.nextLong(),
			random.nextDouble(),
			random.nextDouble(),
			random.nextLong(),
			random.nextLong(),
			random.nextDouble(),
			random.nextDouble()
		);
	}

	static ExecutionMetricsSummaryInfo createRandomExecutionMetricsSummaryInfo(Random random) {
		return new ExecutionMetricsSummaryInfo(
			createRandomMetricSummaryInfo(random),
			createRandomMetricSummaryInfo(random),
			createRandomMetricSummaryInfo(random),
			createRandomMetricSummaryInfo(random),
			createRandomMetricSummaryInfo(random),
			createRandomMetricSummaryInfo(random),
			createRandomMetricSummaryInfo(random),
			createRandomMetricSummaryInfo(random),
			createRandomMetricSummaryInfo(random),
			createRandomMetricSummaryInfo(random),
			createRandomMetricSummaryInfo(random)
		);
	}
}
