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
import org.apache.flink.runtime.execution.ExecutionState;
import org.apache.flink.runtime.executiongraph.AccessExecution;
import org.apache.flink.runtime.executiongraph.AccessExecutionGraph;
import org.apache.flink.runtime.executiongraph.AccessExecutionJobVertex;
import org.apache.flink.runtime.executiongraph.AccessExecutionVertex;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.rest.NotFoundException;
import org.apache.flink.runtime.rest.handler.HandlerRequest;
import org.apache.flink.runtime.rest.handler.legacy.ExecutionGraphCache;
import org.apache.flink.runtime.rest.handler.legacy.metrics.MetricFetcher;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.MessageHeaders;
import org.apache.flink.runtime.rest.messages.ResponseBody;
import org.apache.flink.runtime.rest.messages.job.ExecutionInfo;
import org.apache.flink.runtime.rest.messages.job.ExecutionMetricsInfo;
import org.apache.flink.runtime.rest.messages.job.ExecutorInfo;
import org.apache.flink.runtime.rest.messages.job.JobIDPathParameter;
import org.apache.flink.runtime.rest.messages.job.TaskSummaryInfo;
import org.apache.flink.runtime.rest.messages.job.VertexDetailInfo;
import org.apache.flink.runtime.rest.messages.job.VertexIDPathParameter;
import org.apache.flink.runtime.rest.messages.job.VertexMessageParameters;
import org.apache.flink.runtime.taskmanager.TaskManagerLocation;
import org.apache.flink.runtime.webmonitor.RestfulGateway;
import org.apache.flink.runtime.webmonitor.history.ArchivedJson;
import org.apache.flink.runtime.webmonitor.history.JsonArchivist;
import org.apache.flink.runtime.webmonitor.retriever.GatewayRetriever;

import javax.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Request handler for vertex details.
 */
public class VertexDetailHandler extends AbstractExecutionGraphHandler<VertexDetailInfo, VertexMessageParameters> implements JsonArchivist {

	private final MetricFetcher<? extends RestfulGateway> metricFetcher;

	public VertexDetailHandler(
		GatewayRetriever<? extends RestfulGateway> leaderRetriever,
		Time timeout,
		Map<String, String> responseHeaders,
		MessageHeaders<EmptyRequestBody, VertexDetailInfo, VertexMessageParameters> messageHeaders,
		ExecutionGraphCache executionGraphCache,
		Executor executor,
		MetricFetcher<? extends RestfulGateway> metricFetcher
	) {
		super(
			leaderRetriever,
			timeout,
			responseHeaders,
			messageHeaders,
			executionGraphCache,
			executor
		);

		this.metricFetcher = metricFetcher;
	}

	@Override
	protected VertexDetailInfo handleRequest(
		HandlerRequest<EmptyRequestBody, VertexMessageParameters> request,
		AccessExecutionGraph executionGraph
	) throws NotFoundException {
		JobID jobID = request.getPathParameter(JobIDPathParameter.class);
		JobVertexID jobVertexID = request.getPathParameter(VertexIDPathParameter.class);
		AccessExecutionJobVertex jobVertex = executionGraph.getJobVertex(jobVertexID);

		if (jobVertex == null) {
			throw new NotFoundException(String.format("JobVertex %s not found", jobVertexID));
		}

		return createVertexDetailsInfo(jobID, jobVertex, metricFetcher);
	}

	@Override
	public Collection<ArchivedJson> archiveJsonWithPath(AccessExecutionGraph graph) throws IOException {
		Collection<? extends AccessExecutionJobVertex> vertices = graph.getAllVertices().values();
		List<ArchivedJson> archive = new ArrayList<>(vertices.size());

		for (AccessExecutionJobVertex task : vertices) {
			ResponseBody json = createVertexDetailsInfo(graph.getJobID(), task, null);
			String path = getMessageHeaders().getTargetRestEndpointURL()
				.replace(':' + JobIDPathParameter.KEY, graph.getJobID().toString())
				.replace(':' + VertexIDPathParameter.KEY, task.getJobVertexId().toString());
			archive.add(new ArchivedJson(path, json));
		}

		return archive;
	}

	private static VertexDetailInfo createVertexDetailsInfo(
		JobID jobID,
		AccessExecutionJobVertex jobVertex,
		@Nullable MetricFetcher<?> metricFetcher
	) {
		JobVertexID jobVertexID = jobVertex.getJobVertexId();
		String name = jobVertex.getName();
		int parallelism = jobVertex.getParallelism();
		int maxParallelism = jobVertex.getMaxParallelism();

		Collection<TaskSummaryInfo> taskInfos = new ArrayList<>(parallelism);
		for (AccessExecutionVertex executionVertex : jobVertex.getTaskVertices()) {
			TaskSummaryInfo taskInfo =
				createTaskSummaryInfo(jobID, jobVertexID, executionVertex, metricFetcher);
			taskInfos.add(taskInfo);
		}

		return new VertexDetailInfo(jobVertexID, name, parallelism, maxParallelism, taskInfos);
	}

	private static TaskSummaryInfo createTaskSummaryInfo(
		JobID jobID,
		JobVertexID jobVertexID,
		AccessExecutionVertex executionVertex,
		@Nullable MetricFetcher<?> metricFetcher
	) {
		int index = executionVertex.getParallelSubtaskIndex();
		int numAttempts = -1;

		AccessExecution currentExecution = executionVertex.getCurrentExecutionAttempt();
		ExecutionState status = currentExecution.getState();
		long startTime = currentExecution.getStateTimestamp(ExecutionState.CREATED);
		long endTime = status.isTerminal() ? currentExecution.getStateTimestamp(status) : -1;
		long duration = (endTime >= 0) ? (endTime - startTime) : System.currentTimeMillis() - startTime;

		TaskManagerLocation taskManagerLocation = currentExecution.getAssignedResourceLocation();
		ExecutorInfo executorInfo = taskManagerLocation == null ? null :
			new ExecutorInfo(taskManagerLocation.getResourceID(), taskManagerLocation.getFQDNHostname(), taskManagerLocation.dataPort());
		ExecutionMetricsInfo metricsInfo = ExecutionMetricsUtils.getMetrics(jobID, jobVertexID, currentExecution, metricFetcher);

		ExecutionInfo currentExecutionInfo =
			new ExecutionInfo(
				currentExecution.getAttemptId(),
				currentExecution.getAttemptNumber(),
				startTime,
				endTime,
				duration,
				executorInfo,
				currentExecution.getFailureCauseAsString(),
				status,
				metricsInfo
			);

		return new TaskSummaryInfo(
			index,
			numAttempts,
			currentExecutionInfo
		);
	}
}
