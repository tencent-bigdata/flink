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

package org.apache.flink.runtime.state;

/**
 * Specialized {@link KeyGroupsStateHandle} for states that are local keyed.
 */
public class LocalKeyedKeyGroupsStateHandle extends KeyGroupsStateHandle
		implements LocalKeyedStateHandle {

	private static final long serialVersionUID = -2194187691361383511L;

	/**
	 * @param groupRangeOffsets range of key-group ids that in the state of this handle
	 * @param streamStateHandle handle to the actual state of the key-groups
	 */
	public LocalKeyedKeyGroupsStateHandle(KeyGroupRangeOffsets groupRangeOffsets,
			StreamStateHandle streamStateHandle) {
		super(groupRangeOffsets, streamStateHandle);
	}

	/**
	 *
	 * @param keyGroupRange a key group range to intersect.
	 * @return key-group state over a range that is the intersection between this handle's key-group range and the
	 *          provided key-group range.
	 */
	@Override
	public LocalKeyedKeyGroupsStateHandle getIntersection(KeyGroupRange keyGroupRange) {
		return new LocalKeyedKeyGroupsStateHandle(
			getGroupRangeOffsets().getIntersection(keyGroupRange),
			getDelegateStateHandle());
	}

	@Override
	public String toString() {
		return "LocalKeyedKeyGroupsStateHandle{" +
			"groupRangeOffsets=" + getGroupRangeOffsets() +
			", stateHandle=" + getDelegateStateHandle() +
			'}';
	}

}
