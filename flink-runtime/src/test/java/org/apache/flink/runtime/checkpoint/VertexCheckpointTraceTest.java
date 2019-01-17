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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.flink.runtime.checkpoint.CheckpointTestUtils.createRandomVertexTrace;
import static org.junit.Assert.assertEquals;

public class VertexCheckpointTraceTest {

	private final ThreadLocalRandom random = ThreadLocalRandom.current();

	/**
	 * Verify that task traces are correctly collected.
	 */
	@Test
	public void testCollectTaskTrace() {
		Collection<Integer> tasks = Arrays.asList(1, 3, 5, 7, 9);

		VertexCheckpointTracker tracker = new VertexCheckpointTracker(new HashSet<>(tasks));
		VertexCheckpointTrace trace = tracker.getVertexTrace();
		assertEquals(5, trace.getNumTasks());
		assertEquals(0, trace.getNumAcknowledgedTasks());
		assertEquals(0, trace.getSize());

		int numAcknowledgedTasks = 0;
		long size = 0;
		for (int task : tasks) {
			long taskSize = random.nextLong();
			TaskCheckpointTrace taskTrace = new TaskCheckpointTracker().setSize(taskSize).getTaskTrace();

			tracker.collectTaskTrace(task, taskTrace);
			size += taskSize;
			numAcknowledgedTasks++;

			trace = tracker.getVertexTrace();
			assertEquals(5, trace.getNumTasks());
			assertEquals(numAcknowledgedTasks, trace.getNumAcknowledgedTasks());
			assertEquals(size, trace.getSize());
		}
	}

	/**
	 * Verify that vertex traces are correctly serialized and deserialized.
	 */
	@Test
	public void testIsJavaSerializable() throws Exception {
		VertexCheckpointTrace trace = createRandomVertexTrace(random);
		VertexCheckpointTrace traceCopy = CommonTestUtils.createCopySerializable(trace);
		assertEquals(trace, traceCopy);
		assertEquals(trace.hashCode(), traceCopy.hashCode());
	}

}
