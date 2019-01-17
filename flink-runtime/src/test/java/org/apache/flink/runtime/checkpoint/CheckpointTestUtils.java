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

package org.apache.flink.runtime.checkpoint;

import org.apache.flink.runtime.jobgraph.JobVertexID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class CheckpointTestUtils {

	static TaskCheckpointTrace createRandomTaskTrace(Random random) {
		return new TaskCheckpointTrace(
			random.nextLong(),
			random.nextLong(),
			random.nextLong(),
			random.nextLong(),
			random.nextLong(),
			random.nextLong(),
			random.nextLong()
		);
	}

	static VertexCheckpointTrace createRandomVertexTrace(Random random) {
		int numUnacknowledgedTasks = random.nextInt(10);
		Set<Integer> unacknowledgedTasks = new HashSet<>(numUnacknowledgedTasks);
		for (int i = 0; i < numUnacknowledgedTasks; ++i) {
			unacknowledgedTasks.add(random.nextInt());
		}

		int numAcknowledgedTasks = random.nextInt(10);
		Map<Integer, TaskCheckpointTrace> acknowledgedTaskTraces = new HashMap<>(numAcknowledgedTasks);
		for (int i = 0; i < numAcknowledgedTasks; ++i) {
			int taskIndex = random.nextInt();
			TaskCheckpointTrace taskTrace = createRandomTaskTrace(random);
			acknowledgedTaskTraces.put(taskIndex, taskTrace);
		}

		return new VertexCheckpointTrace(unacknowledgedTasks, acknowledgedTaskTraces);
	}

	static CheckpointTrace createRandomCheckpointTrace(Random random) {
		int numVertices = random.nextInt(4);
		Map<JobVertexID, VertexCheckpointTrace> vertexTraces = new HashMap<>(numVertices);
		for (int i = 0; i < numVertices; ++i) {
			JobVertexID vertexId = new JobVertexID();
			VertexCheckpointTrace vertexTrace = createRandomVertexTrace(random);
			vertexTraces.put(vertexId, vertexTrace);
		}

		return new CheckpointTrace(
			random.nextLong(),
			CheckpointType.values()[random.nextInt(CheckpointType.values().length)],
			CheckpointStatus.values()[random.nextInt(CheckpointType.values().length)],
			random.nextLong(),
			random.nextLong(),
			random.nextLong(),
			"failure" + random.nextInt(),
			vertexTraces
		);
	}
}
