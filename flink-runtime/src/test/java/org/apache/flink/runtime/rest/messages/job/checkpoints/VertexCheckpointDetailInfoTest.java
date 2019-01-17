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

import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.rest.messages.RestResponseMarshallingTestBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Tests the (un)marshalling of {@link VertexCheckpointDetailInfo}.
 */
public class VertexCheckpointDetailInfoTest extends RestResponseMarshallingTestBase<VertexCheckpointDetailInfo> {

	@Override
	protected Class<VertexCheckpointDetailInfo> getTestResponseClass() {
		return VertexCheckpointDetailInfo.class;
	}

	@Override
	protected VertexCheckpointDetailInfo getTestResponseInstance() throws Exception {
		Random random = new Random();

		int numTasks = random.nextInt(20);
		Collection<TaskCheckpointInfo> taskInfos = new ArrayList<>(numTasks);
		for (int i = 0; i < numTasks; ++i) {
			TaskCheckpointInfo taskInfo = createRandomTaskCheckpointInfo(random);
			taskInfos.add(taskInfo);
		}

		return new VertexCheckpointDetailInfo(
			new JobVertexID(),
			random.nextInt(),
			random.nextInt(),
			taskInfos
		);
	}

	private static TaskCheckpointInfo createRandomTaskCheckpointInfo(Random random) {
		return new TaskCheckpointInfo(
			random.nextInt(),
			random.nextLong(),
			random.nextLong(),
			new CheckpointMetricsInfo(
				random.nextLong(),
				random.nextLong(),
				random.nextLong(),
				random.nextLong(),
				random.nextLong()
			)
		);
	}

}
