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

package org.apache.flink.runtime.rest.messages.job.checkpoints;

import org.apache.flink.runtime.checkpoint.CheckpointStatus;
import org.apache.flink.runtime.checkpoint.CheckpointType;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.rest.messages.RestResponseMarshallingTestBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static org.apache.flink.runtime.rest.messages.MessageTestUtils.createRandomMetricSummaryInfo;

/**
 * Tests for {@link CheckpointDetailInfo}.
 */
public class CheckpointDetailInfoTest extends RestResponseMarshallingTestBase<CheckpointDetailInfo> {
	@Override
	protected Class<CheckpointDetailInfo> getTestResponseClass() {
		return CheckpointDetailInfo.class;
	}

	@Override
	protected CheckpointDetailInfo getTestResponseInstance() throws Exception {
		final Random random = new Random();

		int numVertices = random.nextInt(20);
		Collection<VertexCheckpointSummaryInfo> vertexSummaries = new ArrayList<>(numVertices);
		for (int i = 0; i < numVertices; ++i) {
			VertexCheckpointSummaryInfo vertexSummary = createRandomVertexCheckpointSummaryInfo(random);
			vertexSummaries.add(vertexSummary);
		}

		return new CheckpointDetailInfo(
			random.nextLong(),
			CheckpointType.values()[random.nextInt(CheckpointType.values().length)],
			CheckpointStatus.values()[random.nextInt(CheckpointStatus.values().length)],
			random.nextLong(),
			random.nextLong(),
			random.nextLong(),
			random.nextInt(),
			random.nextInt(),
			random.nextLong(),
			"failure" + random.nextInt(),
			vertexSummaries
		);
	}

	private static VertexCheckpointSummaryInfo createRandomVertexCheckpointSummaryInfo(Random random) {
		return new VertexCheckpointSummaryInfo(
			new JobVertexID(),
			random.nextInt(),
			random.nextInt(),
			createRandomCheckpointMetricsSummaryInfo(random)
		);
	}

	private static CheckpointMetricsSummaryInfo createRandomCheckpointMetricsSummaryInfo(Random random) {
		return new CheckpointMetricsSummaryInfo(
			createRandomMetricSummaryInfo(random),
			createRandomMetricSummaryInfo(random),
			createRandomMetricSummaryInfo(random),
			createRandomMetricSummaryInfo(random),
			createRandomMetricSummaryInfo(random)
		);
	}
}
