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

package org.apache.flink.runtime.executiongraph;

import org.apache.flink.util.AbstractID;

import org.apache.flink.shaded.netty4.io.netty.buffer.ByteBuf;
import org.apache.flink.util.StringUtils;

/**
 * Unique identifier for the attempt to execute a tasks. Multiple attempts happen
 * in cases of failures and recovery.
 */
public class ExecutionAttemptID extends AbstractID {

	private static final long serialVersionUID = -1169683445778281344L;

	public ExecutionAttemptID() {
	}

	public ExecutionAttemptID(long lowerPart, long upperPart) {
		super(lowerPart, upperPart);
	}

	public ExecutionAttemptID(byte[] bytes) {
		super(bytes);
	}

	public void writeTo(ByteBuf buf) {
		buf.writeLong(this.lowerPart);
		buf.writeLong(this.upperPart);
	}

	public static ExecutionAttemptID fromByteBuf(ByteBuf buf) {
		long lower = buf.readLong();
		long upper = buf.readLong();
		return new ExecutionAttemptID(lower, upper);
	}

	public static ExecutionAttemptID fromByteArray(byte[] bytes) {
		return new ExecutionAttemptID(bytes);
	}

	/**
	 * Parses a ExecutionAttemptID from the given string.
	 *
	 * @param hexString string representation of a ExecutionAttemptID
	 * @return Parsed ExecutionAttemptID
	 * @throws IllegalArgumentException if the ExecutionAttemptID could not be parsed from the given string
	 */
	public static ExecutionAttemptID fromHexString(String hexString) {
		try {
			return new ExecutionAttemptID(StringUtils.hexStringToByte(hexString));
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot parse ExecutionAttemptID from \"" + hexString + "\".", e);
		}
	}
}
