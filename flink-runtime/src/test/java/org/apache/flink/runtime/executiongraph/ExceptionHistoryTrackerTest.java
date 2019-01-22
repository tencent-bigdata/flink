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
import org.junit.Test;

import java.util.Random;

import static org.apache.flink.runtime.executiongraph.ExceptionTraceTest.createRandomFailureTrace;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ExceptionHistoryTracker}.
 */
public class ExceptionHistoryTrackerTest {

	Random random = new Random();

	@Test
	public void testAccess() {
		ExceptionHistoryTracker exceptionHistoryTracker = new ExceptionHistoryTracker(10);

		ExceptionTracesSnapshot exceptionTracesSnapshot1 = exceptionHistoryTracker.snapshot();
		assertNotNull(exceptionTracesSnapshot1);
		assertEquals(0, exceptionTracesSnapshot1.getNumFailures());
		assertTrue(exceptionTracesSnapshot1.getExceptionTraces().isEmpty());

		ExceptionTrace exceptionTrace = createRandomFailureTrace(random);
		exceptionHistoryTracker.addFailureTrace(exceptionTrace);
		ExceptionTracesSnapshot exceptionTracesSnapshot2 = exceptionHistoryTracker.snapshot();
		assertNotNull(exceptionTracesSnapshot2);
		assertEquals(1, exceptionTracesSnapshot2.getNumFailures());
		assertThat(exceptionTracesSnapshot2.getExceptionTraces(), hasItem(exceptionTrace));
	}

	@Test
	public void testJavaSerializable() throws Exception {
		ExceptionHistoryTracker exceptionHistoryTracker = new ExceptionHistoryTracker(10);

		int numFailures = random.nextInt(10);
		for (int i = 0; i < numFailures; ++i) {
			ExceptionTrace exceptionTrace = createRandomFailureTrace(random);
			exceptionHistoryTracker.addFailureTrace(exceptionTrace);
		}

		ExceptionTracesSnapshot exceptionTracesSnapshot = exceptionHistoryTracker.snapshot();
		ExceptionTracesSnapshot exceptionTracesSnapshotCopy = CommonTestUtils.createCopySerializable(
			exceptionTracesSnapshot);
		assertEquals(exceptionTracesSnapshot, exceptionTracesSnapshotCopy);
		assertEquals(exceptionTracesSnapshot.hashCode(), exceptionTracesSnapshotCopy.hashCode());
	}
}
