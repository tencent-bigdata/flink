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

package org.apache.flink.runtime.io.network.partition;

import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.executiongraph.IntermediateResultPartition;
import org.apache.flink.runtime.jobgraph.IntermediateDataSetID;

import org.apache.flink.shaded.netty4.io.netty.buffer.ByteBuf;

import java.io.Serializable;
import java.util.Objects;

/**
 * Runtime identifier of a produced {@link IntermediateResultPartition}.
 *
 * <p>In failure cases the {@link IntermediateDataSetID} and {@code partitionIndex} is not enough to
 * uniquely identify a result partition. It needs to be associated with the producing task as well
 * to ensure correct tracking of failed/restarted tasks.
 */
public final class ResultPartitionID implements Serializable {

	private static final long serialVersionUID = -902516386203787826L;

	private final IntermediateDataSetID resultId;

	private final int partitionIndex;

	private final ExecutionAttemptID producerId;

	public ResultPartitionID() {
		this(new IntermediateDataSetID(), 0, new ExecutionAttemptID());
	}

	public ResultPartitionID(IntermediateDataSetID resultId, int partitionIndex, ExecutionAttemptID producerId) {
		this.resultId = resultId;
		this.partitionIndex = partitionIndex;
		this.producerId = producerId;
	}

	public IntermediateDataSetID getResultId() {
		return resultId;
	}

	public int getPartitionIndex() {
		return partitionIndex;
	}

	public ExecutionAttemptID getProducerId() {
		return producerId;
	}

	public void writeTo(ByteBuf buf) {
		resultId.writeTo(buf);
		buf.writeInt(partitionIndex);
		producerId.writeTo(buf);
	}

	public static ResultPartitionID fromByteBuf(ByteBuf buf) {
		IntermediateDataSetID resultId = IntermediateDataSetID.fromByteBuf(buf);
		int partitionIndex = buf.readInt();
		ExecutionAttemptID producerId = ExecutionAttemptID.fromByteBuf(buf);
		return new ResultPartitionID(resultId, partitionIndex, producerId);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ResultPartitionID that = (ResultPartitionID) o;
		return partitionIndex == that.partitionIndex &&
			Objects.equals(resultId, that.resultId) &&
			Objects.equals(producerId, that.producerId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resultId, partitionIndex, producerId);
	}

	@Override
	public String toString() {
		return "ResultPartitionID{" +
			"resultId=" + resultId +
			", partitionIndex=" + partitionIndex +
			", producerId=" + producerId +
			'}';
	}
}
