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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CheckpointStatusTest {

	/**
	 * Tests the getters of each status.
	 */
	@Test
	public void testStatusValues() throws Exception {
		CheckpointStatus inProgress = CheckpointStatus.PENDING;
		assertTrue(inProgress.isPending());
		assertFalse(inProgress.isCompleted());
		assertFalse(inProgress.isFailed());

		CheckpointStatus completed = CheckpointStatus.COMPLETED;
		assertFalse(completed.isPending());
		assertTrue(completed.isCompleted());
		assertFalse(completed.isFailed());

		CheckpointStatus failed = CheckpointStatus.FAILED;
		assertFalse(failed.isPending());
		assertFalse(failed.isCompleted());
		assertTrue(failed.isFailed());
	}
}
