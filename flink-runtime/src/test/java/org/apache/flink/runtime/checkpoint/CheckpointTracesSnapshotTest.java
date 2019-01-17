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

import org.apache.flink.core.testutils.CommonTestUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.apache.flink.runtime.checkpoint.CheckpointTestUtils.createRandomCheckpointTrace;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link CheckpointTracesSnapshot}.
 */
public class CheckpointTracesSnapshotTest {

	/**
	 * Tests that the snapshot is actually serializable.
	 */
	@Test
	public void testIsJavaSerializable() throws Exception {
		Random random = new Random();

		int numCheckpoints = random.nextInt(20);
		Map<Long, CheckpointTrace> checkpointTraces = new HashMap<>(numCheckpoints);
		for (int i = 0; i < numCheckpoints; ++i) {
			CheckpointTrace checkpointTrace = createRandomCheckpointTrace(random);
			checkpointTraces.put(checkpointTrace.getCheckpointId(), checkpointTrace);
		}

		CheckpointTracesSnapshot snapshot =
			new CheckpointTracesSnapshot(
				random.nextInt(),
				random.nextInt(),
				random.nextInt(),
				createRandomCheckpointTrace(random),
				createRandomCheckpointTrace(random),
				checkpointTraces
			);

		CheckpointTracesSnapshot snapshotCopy = CommonTestUtils.createCopySerializable(snapshot);
		assertEquals(snapshot, snapshotCopy);
		assertEquals(snapshot.hashCode(), snapshotCopy.hashCode());
	}
}
