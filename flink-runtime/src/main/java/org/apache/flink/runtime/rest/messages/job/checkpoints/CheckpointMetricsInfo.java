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

package org.apache.flink.runtime.rest.messages.job.checkpoints;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * The metrics information for an checkpoint operation.
 */
public class CheckpointMetricsInfo implements Serializable {

	private static final String FIELD_NAME_ALIGN_DURATION = "alignDuration";
	private static final String FIELD_NAME_SYNC_DURATION = "syncDuration";
	private static final String FIELD_NAME_ASYNC_DURATION = "asyncDuration";
	private static final String FIELD_NAME_DURATION = "duration";
	private static final String FIELD_NAME_SIZE = "size";

	@JsonProperty(FIELD_NAME_ALIGN_DURATION)
	private final long alignDuration;

	@JsonProperty(FIELD_NAME_SYNC_DURATION)
	private final long syncDuration;

	@JsonProperty(FIELD_NAME_ASYNC_DURATION)
	private final long asyncDuration;

	@JsonProperty(FIELD_NAME_DURATION)
	private final long duration;

	@JsonProperty(FIELD_NAME_SIZE)
	private final long size;

	@JsonCreator
	public CheckpointMetricsInfo(
		@JsonProperty(FIELD_NAME_ALIGN_DURATION) long alignDuration,
		@JsonProperty(FIELD_NAME_SYNC_DURATION) long syncDuration,
		@JsonProperty(FIELD_NAME_ASYNC_DURATION) long asyncDuration,
		@JsonProperty(FIELD_NAME_DURATION) long duration,
		@JsonProperty(FIELD_NAME_SIZE) long size
	) {
		this.alignDuration = alignDuration;
		this.syncDuration = syncDuration;
		this.asyncDuration = asyncDuration;
		this.duration = duration;
		this.size = size;
	}

	public CheckpointMetricsInfo() {
		this(-1, -1, -1, -1, -1);
	}

	public long getAlignDuration() {
		return alignDuration;
	}

	public long getSyncDuration() {
		return syncDuration;
	}

	public long getAsyncDuration() {
		return asyncDuration;
	}

	public long getDuration() {
		return duration;
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

		CheckpointMetricsInfo that = (CheckpointMetricsInfo) o;
		return Objects.equals(alignDuration, that.alignDuration) &&
			Objects.equals(syncDuration, that.syncDuration) &&
			Objects.equals(asyncDuration, that.asyncDuration) &&
			Objects.equals(duration, that.duration) &&
			Objects.equals(size, that.size);
	}

	@Override
	public int hashCode() {
		return Objects.hash(alignDuration, syncDuration, asyncDuration, duration, size);
	}

	@Override
	public String toString() {
		return "CheckpointMetricsInfo{" +
			"alignDuration=" + alignDuration +
			", syncDuration=" + syncDuration +
			", asyncDuration=" + asyncDuration +
			", duration=" + duration +
			", size=" + size +
			'}';
	}
}
