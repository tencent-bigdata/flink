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

package org.apache.flink.runtime.messages;

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.jobgraph.JobStatus;

import org.apache.flink.runtime.rest.messages.job.JobsOverviewInfo;
import org.apache.flink.runtime.rest.messages.job.JobSummaryInfo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.fail;

public class WebMonitorMessagesTest {

	@Test
	public void testJobSummaryInfo() {
		try {
			Random rnd = new Random();

			JobID id = GenericMessageTester.randomJobId(rnd);
			String name = GenericMessageTester.randomString(rnd);
			long startTime = rnd.nextLong();
			long endTime = startTime + rnd.nextInt();
			JobStatus status = GenericMessageTester.randomJobStatus(rnd);
			int parallelism = rnd.nextInt();
			int maxParallelism = rnd.nextInt();

			JobSummaryInfo msg1 = new JobSummaryInfo(id, name, startTime, endTime, endTime - startTime, parallelism, maxParallelism, status);
			JobSummaryInfo msg2 = new JobSummaryInfo(id, name, startTime, endTime, endTime - startTime, parallelism, maxParallelism, status);

			GenericMessageTester.testMessageInstances(msg1, msg2);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testClusterJobsOverviewInfo() {
		try {
			Random rnd = new Random();

			int numJobSummaries = rnd.nextInt(20) + 1;
			Collection<JobSummaryInfo> jobSummaries = new ArrayList<>();
			for (int i = 0; i < numJobSummaries; ++i) {
				JobSummaryInfo jobSummary = randomJobSummary(rnd);
				jobSummaries.add(jobSummary);
			}

			GenericMessageTester.testMessageInstance(
				new JobsOverviewInfo(
					rnd.nextInt(),
					rnd.nextInt(),
					rnd.nextInt(),
					rnd.nextInt(),
					jobSummaries
				)
			);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private static JobSummaryInfo randomJobSummary(Random rnd) {
		JobID id = GenericMessageTester.randomJobId(rnd);
		String name = GenericMessageTester.randomString(rnd);
		long startTime = rnd.nextLong();
		long endTime = startTime + rnd.nextInt();
		JobStatus status = GenericMessageTester.randomJobStatus(rnd);
		int parallelism = rnd.nextInt();
		int maxParallelism = rnd.nextInt();

		return new JobSummaryInfo(id, name, startTime, endTime, endTime - startTime, parallelism, maxParallelism, status);
	}
}
