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

import java.io.Serializable;
import java.util.Objects;

/**
 * The trace for a task's checkpoint operation.
 */
public class TaskCheckpointTrace implements Serializable {

	private static final long serialVersionUID = 1L;

	/** The instant at which the alignment starts. */
	private final long alignStartInstant;

	/** The instant at which the alignment ends. */
	private final long alignEndInstant;

	/** The instant at which the synchronous part starts. */
	private final long syncStartInstant;

	/** The instant at which the synchronous part ends. */
	private final long syncEndInstant;

	/** The instant at which the asynchronous part starts. */
	private final long asyncStartInstant;

	/** The instant at which the asynchronous part ends. */
	private final long asyncEndInstant;

	/** The size of the checkpoint data. */
	private final long size;

	public TaskCheckpointTrace(
		long alignStartInstant,
		long alignEndInstant,
		long syncStartInstant,
		long syncEndInstant,
		long asyncStartInstant,
		long asyncEndInstant,
		long size
	) {
		this.alignStartInstant = alignStartInstant;
		this.alignEndInstant = alignEndInstant;
		this.syncStartInstant = syncStartInstant;
		this.syncEndInstant = syncEndInstant;
		this.asyncStartInstant = asyncStartInstant;
		this.asyncEndInstant = asyncEndInstant;
		this.size = size;
	}

	public TaskCheckpointTrace() {
		this(-1, -1, -1, -1, -1, -1, -1);
	}

	public long getStartInstant() {
		return alignStartInstant;
	}

	public long getEndInstant() {
		return asyncEndInstant;
	}

	public long getAlignStartInstant() {
		return alignStartInstant;
	}

	public long getAlignEndInstant() {
		return alignEndInstant;
	}

	public long getSyncStartInstant() {
		return syncStartInstant;
	}

	public long getSyncEndInstant() {
		return syncEndInstant;
	}

	public long getAsyncStartInstant() {
		return asyncStartInstant;
	}

	public long getAsyncEndInstant() {
		return asyncEndInstant;
	}

	public long getAlignDuration() {
		return alignEndInstant - alignStartInstant;
	}

	public long getSyncDuration() {
		return syncEndInstant - syncStartInstant;
	}

	public long getAsyncDuration() {
		return asyncEndInstant - asyncStartInstant;
	}

	public long getDuration() {
		return asyncEndInstant - alignStartInstant;
	}

	public long getSize() {
		return size;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TaskCheckpointTrace that = (TaskCheckpointTrace) o;
		return alignStartInstant == that.alignStartInstant &&
			alignEndInstant == that.alignEndInstant &&
			syncStartInstant == that.syncStartInstant &&
			syncEndInstant == that.syncEndInstant &&
			asyncStartInstant == that.asyncStartInstant &&
			asyncEndInstant == that.asyncEndInstant &&
			size == that.size;
	}

	@Override
	public int hashCode() {
		return Objects.hash(alignStartInstant, alignEndInstant, syncStartInstant, syncEndInstant,
			asyncStartInstant, asyncEndInstant, size);
	}

	@Override
	public String toString() {
		return "TaskCheckpointTrace{" +
			", alignStartInstant=" + alignStartInstant +
			", alignEndInstant=" + alignEndInstant +
			", syncStartInstant=" + syncStartInstant +
			", syncEndInstant=" + syncEndInstant +
			", asyncStartInstant=" + asyncStartInstant +
			", asyncEndInstant=" + asyncEndInstant +
			", size=" + size +
			'}';
	}
}
