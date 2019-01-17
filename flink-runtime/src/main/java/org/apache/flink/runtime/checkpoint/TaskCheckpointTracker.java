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

/**
 * The tracker for a task's checkpoint operation.
 */
public class TaskCheckpointTracker {

	/** The instant at which the alignment starts. */
	private long alignStartInstant;

	/** The instant at which the alignment ends. */
	private long alignEndInstant;

	/** The instant at which the synchronous part starts. */
	private long syncStartInstant;

	/** The instant at which the synchronous part ends. */
	private long syncEndInstant;

	/** The instant at which the asynchronous part starts. */
	private long asyncStartInstant;

	/** The instant at which the asynchronous part ends. */
	private long asyncEndInstant;

	/** The size of the checkpoint data. */
	private long size;

	public TaskCheckpointTracker() {
		this.alignStartInstant = -1;
		this.alignEndInstant = -1;
		this.syncStartInstant = -1;
		this.syncEndInstant = -1;
		this.asyncStartInstant = -1;
		this.asyncEndInstant = -1;
		this.size = -1;
	}

	public TaskCheckpointTracker setAlignStartInstant(long alignStartInstant) {
		this.alignStartInstant = alignStartInstant;
		return this;
	}

	public TaskCheckpointTracker setAlignEndInstant(long alignEndInstant) {
		this.alignEndInstant = alignEndInstant;
		return this;
	}

	public TaskCheckpointTracker setSyncStartInstant(long syncStartInstant) {
		this.syncStartInstant = syncStartInstant;
		return this;
	}

	public TaskCheckpointTracker setSyncEndInstant(long syncEndInstant) {
		this.syncEndInstant = syncEndInstant;
		return this;
	}

	public TaskCheckpointTracker setAsyncStartInstant(long asyncStartInstant) {
		this.asyncStartInstant = asyncStartInstant;
		return this;
	}

	public TaskCheckpointTracker setAsyncEndInstant(long asyncEndInstant) {
		this.asyncEndInstant = asyncEndInstant;
		return this;
	}

	public TaskCheckpointTracker setSize(long size) {
		this.size = size;
		return this;
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

	public long getSize() {
		return size;
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

	public TaskCheckpointTrace getTaskTrace() {
		return new TaskCheckpointTrace(
			alignStartInstant,
			alignEndInstant,
			syncStartInstant,
			syncEndInstant,
			asyncStartInstant,
			asyncEndInstant,
			size
		);
	}
}
