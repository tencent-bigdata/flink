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
import org.apache.flink.runtime.checkpoint.CheckpointMetrics;
import org.apache.flink.runtime.checkpoint.TaskStateSnapshot;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.jobmaster.JobMasterGateway;
import org.apache.flink.runtime.messages.checkpoint.DeclineCheckpoint;
import org.apache.flink.runtime.taskexecutor.JobManagerConnection;
import org.apache.flink.runtime.taskexecutor.JobManagerTable;
import org.apache.flink.runtime.taskmanager.CheckpointResponder;
import org.apache.flink.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcCheckpointResponder implements CheckpointResponder {

	private final static Logger LOG = LoggerFactory.getLogger(RpcCheckpointResponder.class);

	private final JobManagerTable jobManagerTable;

	public RpcCheckpointResponder(JobManagerTable jobManagerTable) {
		Preconditions.checkNotNull(jobManagerTable);

		this.jobManagerTable = jobManagerTable;
	}

	@Override
	public void acknowledgeCheckpoint(
		JobID jobId,
		ExecutionAttemptID executionAttemptId,
		long checkpointId,
		CheckpointMetrics checkpointMetrics,
		TaskStateSnapshot subtaskState
	) {
		JobManagerConnection jobManagerConnection = jobManagerTable.get(jobId);
		if (jobManagerConnection == null) {
			throw new RuntimeException("Could not find the connection to the job master.");
		}

		JobMasterGateway jobMasterGateway = jobManagerConnection.getJobManagerGateway();
		jobMasterGateway.acknowledgeCheckpoint(
			jobId,
			executionAttemptId,
			checkpointId,
			checkpointMetrics,
			subtaskState
		);
	}

	@Override
	public void declineCheckpoint(
		JobID jobId,
		ExecutionAttemptID executionAttemptId,
		long checkpointId,
		Throwable cause
	) {
		JobManagerConnection jobManagerConnection = jobManagerTable.get(jobId);
		if (jobManagerConnection == null) {
			throw new RuntimeException("Could not find the connection to the job master.");
		}

		JobMasterGateway jobMasterGateway = jobManagerConnection.getJobManagerGateway();
		jobMasterGateway.declineCheckpoint(new DeclineCheckpoint(jobId, executionAttemptId, checkpointId, cause));
	}
}
