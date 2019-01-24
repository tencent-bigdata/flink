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

import java.util.Map;
import java.util.UUID;

/**
 * Specialized {@link IncrementalKeyedStateHandle} for states that are local keyed.
 */
public class LocalKeyedIncrementalKeyedStateHandle extends IncrementalKeyedStateHandle
		implements LocalKeyedStateHandle {

	private static final long serialVersionUID = 4081913652254161489L;

	public LocalKeyedIncrementalKeyedStateHandle(
			UUID backendIdentifier,
			KeyGroupRange keyGroupRange,
			long checkpointId,
			Map<StateHandleID, StreamStateHandle> sharedState,
			Map<StateHandleID, StreamStateHandle> privateState,
			StreamStateHandle metaStateHandle) {
		super(backendIdentifier, keyGroupRange, checkpointId, sharedState, privateState,
			metaStateHandle);
	}

	@Override
	public String toString() {
		return "LocalKeyedIncrementalKeyedStateHandle{" +
			"backendIdentifier=" + getBackendIdentifier() +
			", keyGroupRange=" + getKeyGroupRange() +
			", checkpointId=" + getCheckpointId() +
			", sharedState=" + getSharedState() +
			", privateState=" + getPrivateState() +
			", metaStateHandle=" + getMetaStateHandle() +
			", registered=" + (getSharedStateRegistry() != null) +
			'}';
	}

}
