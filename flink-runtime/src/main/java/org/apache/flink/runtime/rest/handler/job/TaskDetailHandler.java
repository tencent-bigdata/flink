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

package org.apache.flink.runtime.rest.handler.job;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.executiongraph.AccessExecution;
import org.apache.flink.runtime.executiongraph.AccessExecutionVertex;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.rest.handler.HandlerRequest;
import org.apache.flink.runtime.rest.handler.RestHandlerException;
import org.apache.flink.runtime.rest.handler.legacy.ExecutionGraphCache;
import org.apache.flink.runtime.rest.handler.legacy.metrics.MetricFetcher;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.MessageHeaders;
import org.apache.flink.runtime.rest.messages.job.ExecutionInfo;
import org.apache.flink.runtime.rest.messages.job.JobIDPathParameter;
import org.apache.flink.runtime.rest.messages.job.TaskDetailInfo;
import org.apache.flink.runtime.rest.messages.job.TaskMessageParameters;
import org.apache.flink.runtime.rest.messages.job.VertexIDPathParameter;
import org.apache.flink.runtime.webmonitor.RestfulGateway;
import org.apache.flink.runtime.webmonitor.retriever.GatewayRetriever;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Request handler providing details for a single task.
 */
public class TaskDetailHandler extends AbstractTaskHandler<TaskDetailInfo, TaskMessageParameters> {

	private final MetricFetcher<? extends RestfulGateway> metricFetcher;

	public TaskDetailHandler(
		GatewayRetriever<? extends RestfulGateway> leaderRetriever,
		Time timeout,
		Map<String, String> responseHeaders,
		MessageHeaders<EmptyRequestBody, TaskDetailInfo, TaskMessageParameters> messageHeaders,
		ExecutionGraphCache executionGraphCache,
		Executor executor,
		MetricFetcher<? extends RestfulGateway> metricFetcher
	) {
		super(leaderRetriever, timeout, responseHeaders, messageHeaders, executionGraphCache, executor);

		this.metricFetcher = metricFetcher;
	}

	@Override
	protected TaskDetailInfo handleRequest(
		HandlerRequest<EmptyRequestBody, TaskMessageParameters> request,
		AccessExecutionVertex executionVertex
	) throws RestHandlerException {

		final JobID jobID = request.getPathParameter(JobIDPathParameter.class);
		final JobVertexID jobVertexID = request.getPathParameter(VertexIDPathParameter.class);
		final int index = executionVertex.getParallelSubtaskIndex();

		final int numAttempts = -1;

		Collection<ExecutionInfo> executionInfos = new ArrayList<>();

		Collection<? extends AccessExecution> priorExecutions = executionVertex.getPriorExecutionAttempts();
		for (AccessExecution priorExecution : priorExecutions) {
			ExecutionInfo priorExecutionInfo = ExecutionInfo.create(jobID, jobVertexID, priorExecution, metricFetcher);
			executionInfos.add(priorExecutionInfo);
		}

		AccessExecution currentExecution = executionVertex.getCurrentExecutionAttempt();
		ExecutionInfo currentExecutionInfo =
			currentExecution == null ? null : ExecutionInfo.create(jobID, jobVertexID, currentExecution, metricFetcher);
		executionInfos.add(currentExecutionInfo);

		return new TaskDetailInfo(index, numAttempts, executionInfos);
	}
}

