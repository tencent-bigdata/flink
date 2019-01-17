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

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CheckpointHistoryTrackerTest {

	/**
	 * Tests tracking of checkpoints.
	 */
	@Test
	public void testAddCheckpoint() {
		CheckpointHistoryTracker checkpointHistoryTracker =
			new CheckpointHistoryTracker(10);
		assertEquals(0, checkpointHistoryTracker.getNumCompletedCheckpoints());
		assertEquals(0, checkpointHistoryTracker.getNumFailedCheckpoints());
		assertNull(checkpointHistoryTracker.getLastCompletedCheckpointTrace());
		assertNull(checkpointHistoryTracker.getLastFailedCheckpointTrace());
		assertNotNull(checkpointHistoryTracker.getCheckpointTraces());
		assertTrue(checkpointHistoryTracker.getCheckpointTraces().isEmpty());

		// Add a failed checkpoint
		CheckpointTrace checkpointTracker1 = createCheckpointTrace(1, CheckpointStatus.FAILED);
		checkpointHistoryTracker.addCheckpointTrace(checkpointTracker1);

		assertEquals(0, checkpointHistoryTracker.getNumCompletedCheckpoints());
		assertEquals(1, checkpointHistoryTracker.getNumFailedCheckpoints());
		assertNull(checkpointHistoryTracker.getLastCompletedCheckpointTrace());
		assertEquals(checkpointTracker1, checkpointHistoryTracker.getLastFailedCheckpointTrace());
		assertEquals(1, checkpointHistoryTracker.getCheckpointTraces().size());

		// Add a completed checkpoint
		CheckpointTrace checkpointTrace2 = createCheckpointTrace(2, CheckpointStatus.COMPLETED);
		checkpointHistoryTracker.addCheckpointTrace(checkpointTrace2);

		assertEquals(1, checkpointHistoryTracker.getNumCompletedCheckpoints());
		assertEquals(1, checkpointHistoryTracker.getNumFailedCheckpoints());
		assertEquals(checkpointTrace2, checkpointHistoryTracker.getLastCompletedCheckpointTrace());
		assertEquals(checkpointTracker1, checkpointHistoryTracker.getLastFailedCheckpointTrace());
		assertEquals(2, checkpointHistoryTracker.getCheckpointTraces().size());
	}

	private static CheckpointTrace createCheckpointTrace(
		long checkpointId,
		CheckpointStatus checkpointStatus
	) {
		return new CheckpointTrace(
			checkpointId,
			CheckpointType.CHECKPOINT,
			checkpointStatus,
			0,
			0,
			0,
			null,
			Collections.emptyMap()
		);
	}

}
