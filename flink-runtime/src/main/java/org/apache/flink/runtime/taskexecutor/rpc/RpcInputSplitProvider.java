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

package org.apache.flink.runtime.taskexecutor.rpc;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.core.io.InputSplit;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.jobgraph.tasks.InputSplitProvider;
import org.apache.flink.runtime.jobgraph.tasks.InputSplitProviderException;
import org.apache.flink.runtime.jobmaster.JobMasterGateway;
import org.apache.flink.runtime.jobmaster.SerializedInputSplit;
import org.apache.flink.runtime.taskexecutor.JobManagerConnection;
import org.apache.flink.runtime.taskexecutor.JobManagerTable;
import org.apache.flink.util.FlinkException;
import org.apache.flink.util.InstantiationUtil;
import org.apache.flink.util.Preconditions;

import java.util.concurrent.CompletableFuture;

public class RpcInputSplitProvider implements InputSplitProvider {
	private final JobManagerTable jobManagerTable;
	private final JobID jobId;
	private final JobVertexID vertexId;
	private final ExecutionAttemptID executionId;
	private final Time timeout;

	public RpcInputSplitProvider(
		JobManagerTable jobManagerTable,
		JobID jobId,
		JobVertexID vertexId,
		ExecutionAttemptID executionId,
		Time timeout
	) {
		this.jobManagerTable = Preconditions.checkNotNull(jobManagerTable);
		this.jobId = Preconditions.checkNotNull(jobId);
		this.vertexId = Preconditions.checkNotNull(vertexId);
		this.executionId = Preconditions.checkNotNull(executionId);
		this.timeout = Preconditions.checkNotNull(timeout);
	}


	@Override
	public InputSplit getNextInputSplit(
		ClassLoader userCodeClassLoader
	) throws InputSplitProviderException {
		Preconditions.checkNotNull(userCodeClassLoader);

		try {

			JobManagerConnection jobManagerConnection = jobManagerTable.get(jobId);
			if (jobManagerConnection == null) {
				throw new FlinkException("Cannot find the connection to the job master.");
			}

			JobMasterGateway jobMasterGateway = jobManagerConnection.getJobManagerGateway();
			CompletableFuture<SerializedInputSplit> futureInputSplit =
				jobMasterGateway.requestNextInputSplit(vertexId, executionId);

			SerializedInputSplit serializedInputSplit = futureInputSplit.get(timeout.getSize(), timeout.getUnit());

			if (serializedInputSplit.isEmpty()) {
				return null;
			} else {
				return InstantiationUtil.deserializeObject(serializedInputSplit.getInputSplitData(), userCodeClassLoader);
			}
		} catch (Exception e) {
			throw new InputSplitProviderException("Requesting the next input split failed.", e);
		}
	}
}
