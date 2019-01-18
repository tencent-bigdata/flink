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

package org.apache.flink.runtime.executiongraph;

import org.apache.flink.core.testutils.CommonTestUtils;
import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.taskmanager.TaskManagerLocation;
import org.junit.Test;

import java.net.InetAddress;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link ExceptionTrace}.
 */
public class ExceptionTraceTest {

	@Test
	public void testJavaSerializable() throws Exception {
		Random random = new Random();
		ExceptionTrace exceptionTrace = createRandomFailureTrace(random);

		ExceptionTrace exceptionTraceCopy = CommonTestUtils.createCopySerializable(exceptionTrace);
		assertEquals(exceptionTrace, exceptionTraceCopy);
		assertEquals(exceptionTrace.hashCode(), exceptionTraceCopy.hashCode());
	}

	static ExceptionTrace createRandomFailureTrace(Random random) {
		return new ExceptionTrace(
			random.nextLong(),
			new Exception("exception" + random.nextInt()),
			new ExecutionAttemptID(),
			"task" + random.nextInt(),
			random.nextInt(),
			new TaskManagerLocation(new ResourceID("resource" + random.nextInt()), InetAddress.getLoopbackAddress(), random.nextInt(1024) + 1)
		);
	}
}
