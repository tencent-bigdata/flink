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

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.jobgraph.JobStatus;
import org.apache.flink.runtime.rest.messages.RestResponseMarshallingTestBase;

import java.util.Arrays;

/**
 * Tests for the {@link JobsOverviewInfo}.
 */
public class JobsOverviewInfoTest extends RestResponseMarshallingTestBase<JobsOverviewInfo> {

	@Override
	protected Class<JobsOverviewInfo> getTestResponseClass() {
		return JobsOverviewInfo.class;
	}

	@Override
	protected JobsOverviewInfo getTestResponseInstance() throws Exception {
		final JobSummaryInfo running =
			new JobSummaryInfo(
				new JobID(),
				"running",
				1L,
				10L,
				9L,
				16,
				64,
				JobStatus.RUNNING
			);

		final JobSummaryInfo finished =
			new JobSummaryInfo(
				new JobID(),
				"finished",
				1L,
				5L,
				4L,
				8,
				8,
				JobStatus.FINISHED
			);

		return new JobsOverviewInfo(
			1,
			1,
			0,
			0,
			Arrays.asList(running, finished)
		);
	}
}
