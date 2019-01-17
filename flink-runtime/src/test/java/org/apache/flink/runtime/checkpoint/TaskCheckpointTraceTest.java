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

import java.util.concurrent.ThreadLocalRandom;

import static org.apache.flink.runtime.checkpoint.CheckpointTestUtils.createRandomTaskTrace;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link TaskCheckpointTrace}.
 */
public class TaskCheckpointTraceTest {

	private final ThreadLocalRandom random = ThreadLocalRandom.current();

	/**
	 * Tests simple access via the getters.
	 */
	@Test
	public void testSimpleAccess() {

		TaskCheckpointTracker tracker = new TaskCheckpointTracker();
		tracker.setAlignStartInstant(Integer.MAX_VALUE + 1L)
			.setAlignEndInstant(Integer.MAX_VALUE + 2L)
			.setSyncStartInstant(Integer.MAX_VALUE + 3L)
			.setSyncEndInstant(Integer.MAX_VALUE + 4L)
			.setAsyncStartInstant(Integer.MAX_VALUE + 5L)
			.setAsyncEndInstant(Integer.MAX_VALUE + 6L)
			.setSize(Integer.MAX_VALUE + 7L);

		TaskCheckpointTrace trace = tracker.getTaskTrace();

		assertEquals(Integer.MAX_VALUE + 1L, trace.getStartInstant());
		assertEquals(Integer.MAX_VALUE + 6L, trace.getEndInstant());
		assertEquals(Integer.MAX_VALUE + 1L, trace.getAlignStartInstant());
		assertEquals(Integer.MAX_VALUE + 2L, trace.getAlignEndInstant());
		assertEquals(Integer.MAX_VALUE + 3L, trace.getSyncStartInstant());
		assertEquals(Integer.MAX_VALUE + 4L, trace.getSyncEndInstant());
		assertEquals(Integer.MAX_VALUE + 5L, trace.getAsyncStartInstant());
		assertEquals(Integer.MAX_VALUE + 6L, trace.getAsyncEndInstant());
		assertEquals(1, trace.getAlignDuration());
		assertEquals(1, trace.getSyncDuration());
		assertEquals(1, trace.getAsyncDuration());
		assertEquals(5, trace.getDuration());
		assertEquals(Integer.MAX_VALUE + 7L, trace.getSize());
	}

	/**
	 * Tests that the snapshot is actually serializable.
	 */
	@Test
	public void testIsJavaSerializable() throws Exception {
		TaskCheckpointTrace trace = createRandomTaskTrace(random);
		TaskCheckpointTrace traceCopy = CommonTestUtils.createCopySerializable(trace);
		assertEquals(trace, traceCopy);
		assertEquals(trace.hashCode(), traceCopy.hashCode());
	}

}
