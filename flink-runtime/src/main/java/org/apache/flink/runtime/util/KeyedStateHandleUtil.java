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

package org.apache.flink.runtime.util;

import org.apache.flink.runtime.state.IncrementalKeyedStateHandle;
import org.apache.flink.runtime.state.KeyGroupRange;
import org.apache.flink.runtime.state.KeyGroupRangeOffsets;
import org.apache.flink.runtime.state.KeyGroupsStateHandle;
import org.apache.flink.runtime.state.KeyScope;
import org.apache.flink.runtime.state.LocalKeyedIncrementalKeyedStateHandle;
import org.apache.flink.runtime.state.LocalKeyedKeyGroupsStateHandle;
import org.apache.flink.runtime.state.StateHandleID;
import org.apache.flink.runtime.state.StreamStateHandle;

import java.util.Map;
import java.util.UUID;

/**
 * Utilities for keyed state handle.
 */
public class KeyedStateHandleUtil {

	public static KeyGroupsStateHandle newKeyGroupsStateHandle(KeyGroupRangeOffsets keyGroupRangeOffsets,
			StreamStateHandle streamStateHandle,
			KeyScope keyScope) {
		switch (keyScope) {
			case GLOBAL:
				return new KeyGroupsStateHandle(keyGroupRangeOffsets, streamStateHandle);
			case LOCAL:
				return new LocalKeyedKeyGroupsStateHandle(keyGroupRangeOffsets, streamStateHandle);
		}
		throw new IllegalArgumentException("Unknown key scope encountered: " + keyScope.name());
	}

	public static IncrementalKeyedStateHandle newIncrementalKeyedStateHandle(
			UUID backendIdentifier,
			KeyGroupRange keyGroupRange,
			long checkpointId,
			Map<StateHandleID, StreamStateHandle> sharedState,
			Map<StateHandleID, StreamStateHandle> privateState,
			StreamStateHandle metaStateHandle,
			KeyScope keyScope) {
		switch (keyScope) {
			case GLOBAL:
				return new IncrementalKeyedStateHandle(
					backendIdentifier,
					keyGroupRange,
					checkpointId,
					sharedState,
					privateState,
					metaStateHandle);
			case LOCAL:
				return new LocalKeyedIncrementalKeyedStateHandle(
					backendIdentifier,
					keyGroupRange,
					checkpointId,
					sharedState,
					privateState,
					metaStateHandle);
		}
		throw new IllegalArgumentException("Unknown key scope encountered: " + keyScope.name());
	}

}
