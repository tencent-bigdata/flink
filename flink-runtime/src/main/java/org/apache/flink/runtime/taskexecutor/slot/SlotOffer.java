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

package org.apache.flink.runtime.taskexecutor.slot;

import org.apache.flink.runtime.clusterframework.types.AllocationID;
import org.apache.flink.runtime.clusterframework.types.ResourceProfile;
import org.apache.flink.runtime.taskmanager.TaskExecutionState;
import org.apache.flink.util.Preconditions;

import java.io.Serializable;
import java.util.Collection;

/**
 * Describe the slot offering to job manager provided by task manager.
 */
public class SlotOffer implements Serializable {

	private static final long serialVersionUID = -7067814231108250971L;

	/** Allocation id of this slot, this would be the only identifier for this slot offer */
	private AllocationID allocationId;

	/** Index of the offered slot */
	private final int slotIndex;

	/** The resource profile of the offered slot */
	private final ResourceProfile resourceProfile;

	/** The execution states of the tasks in the slot (if any). */
	private final Collection<TaskExecutionState> taskExecutionStates;

	public SlotOffer(
		final AllocationID allocationID,
		final int slotIndex,
		final ResourceProfile resourceProfile,
		final Collection<TaskExecutionState> taskExecutionStates
	) {
		Preconditions.checkNotNull(allocationID);
		Preconditions.checkArgument(0 <= slotIndex, "The slotIndex must be greater than 0.");
		Preconditions.checkNotNull(resourceProfile);
		Preconditions.checkNotNull(taskExecutionStates);

		this.allocationId = allocationID;
		this.slotIndex = slotIndex;
		this.resourceProfile = resourceProfile;
		this.taskExecutionStates = taskExecutionStates;
	}

	public AllocationID getAllocationId() {
		return allocationId;
	}

	public int getSlotIndex() {
		return slotIndex;
	}

	public ResourceProfile getResourceProfile() {
		return resourceProfile;
	}

	public Collection<TaskExecutionState> getTaskExecutionStates() {
		return taskExecutionStates;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		SlotOffer slotOffer = (SlotOffer) o;
		return allocationId.equals(slotOffer.allocationId);
	}

	@Override
	public int hashCode() {
		return allocationId.hashCode();
	}
}
