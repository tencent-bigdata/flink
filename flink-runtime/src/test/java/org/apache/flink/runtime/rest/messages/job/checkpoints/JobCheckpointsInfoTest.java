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
import org.apache.flink.runtime.rest.messages.RestResponseMarshallingTestBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Tests for {@link JobCheckpointsInfo}.
 */
public class JobCheckpointsInfoTest extends RestResponseMarshallingTestBase<JobCheckpointsInfo> {
	@Override
	protected Class<JobCheckpointsInfo> getTestResponseClass() {
		return JobCheckpointsInfo.class;
	}

	@Override
	protected JobCheckpointsInfo getTestResponseInstance() throws Exception {
		Random random = new Random();

		int numCheckpoints = random.nextInt(20);
		Collection<CheckpointSummaryInfo> checkpointSummaries = new ArrayList<>(numCheckpoints);
		for (int i = 0; i < numCheckpoints; ++i) {
			CheckpointSummaryInfo checkpointSummary = createRandomCheckpointSummaryInfo(random);
			checkpointSummaries.add(checkpointSummary);
		}

		return new JobCheckpointsInfo(
			random.nextInt(),
			random.nextInt(),
			random.nextInt(),
			createRandomCheckpointSummaryInfo(random),
			createRandomCheckpointSummaryInfo(random),
			checkpointSummaries
		);
	}

	private static CheckpointSummaryInfo createRandomCheckpointSummaryInfo(Random random) {
		return new CheckpointSummaryInfo(
			random.nextLong(),
			CheckpointType.values()[random.nextInt(CheckpointType.values().length)],
			CheckpointStatus.values()[random.nextInt(CheckpointStatus.values().length)],
			random.nextLong(),
			random.nextLong(),
			random.nextLong(),
			random.nextInt(),
			random.nextInt(),
			random.nextLong(),
			"failure" + random.nextInt()
		);
	}
}
