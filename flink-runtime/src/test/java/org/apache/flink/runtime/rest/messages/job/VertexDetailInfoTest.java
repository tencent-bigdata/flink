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

import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.rest.messages.RestResponseMarshallingTestBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.apache.flink.runtime.rest.messages.job.JobMessageTestUtils.createRandomExecutionInfo;

/**
 * Tests that the {@link VertexDetailInfo} can be marshalled and unmarshalled.
 */
public class VertexDetailInfoTest extends RestResponseMarshallingTestBase<VertexDetailInfo> {
	@Override
	protected Class<VertexDetailInfo> getTestResponseClass() {
		return VertexDetailInfo.class;
	}

	@Override
	protected VertexDetailInfo getTestResponseInstance() throws Exception {
		final Random random = new Random();

		List<TaskSummaryInfo> taskSummaries = new ArrayList<>();
		taskSummaries.add(createRandomTaskSummaryInfo(random));
		taskSummaries.add(createRandomTaskSummaryInfo(random));
		taskSummaries.add(createRandomTaskSummaryInfo(random));

		return new VertexDetailInfo(
			new JobVertexID(),
			"jobVertex" + random.nextLong(),
			random.nextInt(),
			random.nextInt(),
			taskSummaries);
	}

	private static TaskSummaryInfo createRandomTaskSummaryInfo(Random random) {

		return new TaskSummaryInfo(
			random.nextInt(),
			random.nextInt(),
			createRandomExecutionInfo(random)
		);
	}
}
