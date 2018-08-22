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

package org.apache.flink.streaming.api.windowing.windows;

import org.apache.flink.api.common.typeutils.base.TypeSerializerSingleton;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;

import java.io.IOException;

/**
 * Custom time window.
 */
public class CustomTimeWindow extends TimeWindow implements Comparable<CustomTimeWindow> {

	private long timeoutTs; // 窗口过期的时间戳
	private long arriveTs;
	private boolean isLate = false;

	public CustomTimeWindow(long start, long end) {
		super(start, end);
	}

	public CustomTimeWindow(long start, long end, long timeoutTs) {
		super(start, end);
		this.timeoutTs = timeoutTs;
	}

	public CustomTimeWindow(long start, long end, long timeoutTs, long arriveTs) {
		super(start, end);
		this.timeoutTs = timeoutTs;
		this.arriveTs = arriveTs;
	}

	public CustomTimeWindow(long start, long end, long timeoutTs, long arriveTs,
		boolean isLate) {
		super(start, end);
		this.timeoutTs = timeoutTs;
		this.arriveTs = arriveTs;
		this.isLate = isLate;
	}

	public long getTimeoutTs() {
		return timeoutTs;
	}

	public void setTimeoutTs(long timeoutTs) {
		this.timeoutTs = timeoutTs;
	}

	public long getArriveTs() {
		return arriveTs;
	}

	public void setArriveTs(long arriveTs) {
		this.arriveTs = arriveTs;
	}

	public boolean isLate() {
		return isLate;
	}

	public void setLate(boolean isLate) {
		this.isLate = isLate;
	}


	/**
	 * The serializer used to write the TimeWindow type.
	 */
	public static class Serializer
		extends TypeSerializerSingleton<CustomTimeWindow> {

		private static final long serialVersionUID = 1L;

		@Override
		public boolean isImmutableType() {
			return true;
		}

		@Override
		public CustomTimeWindow createInstance() {
			return null;
		}

		@Override
		public CustomTimeWindow copy(CustomTimeWindow from) {
			return from;
		}

		@Override
		public CustomTimeWindow copy(CustomTimeWindow from, CustomTimeWindow reuse) {
			return from;
		}

		@Override
		public int getLength() {
			return 0;
		}

		@Override
		public void serialize(CustomTimeWindow record, DataOutputView target)
			throws IOException {
			target.writeLong(record.getStart());
			target.writeLong(record.getEnd());
			target.writeLong(record.getTimeoutTs());
			target.writeLong(record.getArriveTs());
			target.writeBoolean(record.isLate());
		}

		@Override
		public CustomTimeWindow deserialize(DataInputView source) throws IOException {
			long start = source.readLong();
			long end = source.readLong();
			long timeoutTs = source.readLong();
			long arriveTs = source.readLong();
			boolean isLate = source.readBoolean();

			return new CustomTimeWindow(start, end, timeoutTs, arriveTs, isLate);
		}

		@Override
		public CustomTimeWindow deserialize(CustomTimeWindow reuse,
			DataInputView source) throws IOException {
			return deserialize(source);
		}

		@Override
		public void copy(DataInputView source, DataOutputView target)
			throws IOException {
			target.writeLong(source.readLong());
			target.writeLong(source.readLong());
			target.writeLong(source.readLong());
			target.writeLong(source.readLong());
			target.writeBoolean(source.readBoolean());
		}

		@Override
		public boolean canEqual(Object obj) {
			return obj instanceof Serializer;
		}

	}

	@Override
	public int compareTo(CustomTimeWindow o) {
		if (this.getStart() > o.getStart()) {
			return 1;
		} else if (this.getStart() < o.getStart()) {
			return -1;
		}
		return 0;
	}

	@Override
	public String toString() {
		return "CustomTimeWindow [arriveTs=" + arriveTs;
	}

	public long getWindowSize(){
		return this.getEnd() + 1 - this.getStart();
	}
}
