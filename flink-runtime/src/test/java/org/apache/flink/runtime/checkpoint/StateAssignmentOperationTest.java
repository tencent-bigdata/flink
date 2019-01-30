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

import org.apache.flink.runtime.jobgraph.OperatorID;
import org.apache.flink.runtime.state.KeyGroupRange;
import org.apache.flink.runtime.state.KeyedStateHandle;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link StateAssignmentOperation}.
 */
public class StateAssignmentOperationTest {

	@Test
	public void testReDistributeLocalKeyedStates1_2() throws Exception {
		final int maxParallelism = 7;

		// parallelism: 1 -> 2
		int oldParallelism = 1;
		OperatorState operatorState = newOperatorState(oldParallelism, maxParallelism);

		int newParallelism = 2;
		List<KeyedStateHandle>[] reDistributed =
			StateAssignmentOperation.reDistributeLocalKeyedStates(operatorState, newParallelism);

		assertEquals(newParallelism, reDistributed.length);
		assertEquals(1, reDistributed[0].size());
		assertEquals(1, reDistributed[1].size());

		assertEquals(new KeyGroupRange(0, 2), reDistributed[0].get(0).getKeyGroupRange());

		assertEquals(new KeyGroupRange(3, 6), reDistributed[1].get(0).getKeyGroupRange());
	}

	@Test
	public void testReDistributeLocalKeyedStates2_1() throws Exception {
		final int maxParallelism = 7;
		final KeyGroupRange fullRange = new KeyGroupRange(0, maxParallelism - 1);

		// parallelism: 2 -> 1
		int oldParallelism = 2;
		OperatorState operatorState = newOperatorState(oldParallelism, maxParallelism);

		int newParallelism = 1;
		List<KeyedStateHandle>[] reDistributed =
			StateAssignmentOperation.reDistributeLocalKeyedStates(operatorState, newParallelism);

		assertEquals(newParallelism, reDistributed.length);
		assertEquals(2, reDistributed[0].size());

		assertEquals(fullRange, reDistributed[0].get(0).getKeyGroupRange());
		assertEquals(fullRange, reDistributed[0].get(1).getKeyGroupRange());
	}

	@Test
	public void testReDistributeLocalKeyedStates2_3() throws Exception {
		final int maxParallelism = 7;

		// parallelism: 2 -> 3
		int oldParallelism = 2;
		OperatorState operatorState = newOperatorState(oldParallelism, maxParallelism);

		int newParallelism = 3;
		List<KeyedStateHandle>[] reDistributed =
			StateAssignmentOperation.reDistributeLocalKeyedStates(operatorState, newParallelism);

		assertEquals(newParallelism, reDistributed.length);
		assertEquals(1, reDistributed[0].size());
		assertEquals(2, reDistributed[1].size());
		assertEquals(1, reDistributed[2].size());

		assertEquals(new KeyGroupRange(0, 3), reDistributed[0].get(0).getKeyGroupRange());

		assertEquals(new KeyGroupRange(4, 6), reDistributed[1].get(0).getKeyGroupRange());
		assertEquals(new KeyGroupRange(0, 0), reDistributed[1].get(1).getKeyGroupRange());

		assertEquals(new KeyGroupRange(1, 6), reDistributed[2].get(0).getKeyGroupRange());
	}

	@Test
	public void testReDistributeLocalKeyedStates5_2() throws Exception {
		final int maxParallelism = 7;
		final KeyGroupRange fullRange = new KeyGroupRange(0, maxParallelism - 1);

		// parallelism: 5 -> 2
		int oldParallelism = 5;
		OperatorState operatorState = newOperatorState(oldParallelism, maxParallelism);

		int newParallelism = 2;
		List<KeyedStateHandle>[] reDistributed =
			StateAssignmentOperation.reDistributeLocalKeyedStates(operatorState, newParallelism);

		assertEquals(newParallelism, reDistributed.length);
		assertEquals(3, reDistributed[0].size());
		assertEquals(3, reDistributed[1].size());

		assertEquals(fullRange, reDistributed[0].get(0).getKeyGroupRange());
		assertEquals(fullRange, reDistributed[0].get(1).getKeyGroupRange());
		assertEquals(new KeyGroupRange(0, 2), reDistributed[0].get(2).getKeyGroupRange());

		assertEquals(new KeyGroupRange(3, 6), reDistributed[1].get(0).getKeyGroupRange());
		assertEquals(fullRange, reDistributed[1].get(1).getKeyGroupRange());
		assertEquals(fullRange, reDistributed[1].get(2).getKeyGroupRange());
	}

	private OperatorState newOperatorState(int parallelism, int maxParallelism) {
		final KeyGroupRange fullRange = new KeyGroupRange(0, maxParallelism - 1);
		OperatorState operatorState = new OperatorState(new OperatorID(), parallelism, maxParallelism);
		for (int i = 0; i < parallelism; i++) {
			OperatorSubtaskState subTaskState = new OperatorSubtaskState(
				null, null, StateHandleDummyUtil.createNewLocalKeyedStateHandle(fullRange), null);
			operatorState.putState(i, subTaskState);
		}
		return operatorState;
	}

}
