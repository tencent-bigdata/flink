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
import org.apache.flink.runtime.execution.ExecutionState;
import org.apache.flink.runtime.jobgraph.JobStatus;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.rest.messages.RestResponseMarshallingTestBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.apache.flink.runtime.rest.messages.job.JobMessageTestUtils.createRandomExecutionMetricsSummaryInfo;

/**
 * Tests (un)marshalling of the {@link JobDetailInfo}.
 */
public class JobDetailInfoTest extends RestResponseMarshallingTestBase<JobDetailInfo> {

	@Override
	protected Class<JobDetailInfo> getTestResponseClass() {
		return JobDetailInfo.class;
	}

	@Override
	protected JobDetailInfo getTestResponseInstance() throws Exception {
		final Random random = new Random();
		final String jsonPlan = "{\"id\":\"1234\"}";

		final int numVertices = 4;
		final Collection<VertexSummaryInfo> vertexInfos = new ArrayList<>(numVertices);
		for (int i = 0; i < numVertices; i++) {
			vertexInfos.add(createVertexSummaryInfo(random));
		}

		return new JobDetailInfo(
			new JobID(),
			"foobar",
			1L,
			-1L,
			1L,
			JobStatus.RUNNING,
			jsonPlan,
			vertexInfos
		);
	}

	private static VertexSummaryInfo createVertexSummaryInfo(Random random) {

		final Map<ExecutionState, Integer> numTasksPerState = new HashMap<>(ExecutionState.values().length);
		for (ExecutionState executionState : ExecutionState.values()) {
			numTasksPerState.put(executionState, random.nextInt());
		}

		return new VertexSummaryInfo(
			new JobVertexID(),
			random.nextInt(),
			"jobVertex" + random.nextLong(),
			random.nextLong(),
			random.nextLong(),
			random.nextLong(),
			random.nextInt(),
			random.nextInt(),
			numTasksPerState,
			ExecutionState.values()[random.nextInt(ExecutionState.values().length)],
			createRandomExecutionMetricsSummaryInfo(random)
		);
	}
}
