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
import org.apache.flink.runtime.executiongraph.AccessExecutionGraph;
import org.apache.flink.runtime.executiongraph.AccessExecutionJobVertex;
import org.apache.flink.runtime.executiongraph.AccessExecutionVertex;
import org.apache.flink.runtime.executiongraph.ExecutionJobVertex;
import org.apache.flink.runtime.jobgraph.JobStatus;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.rest.handler.HandlerRequest;
import org.apache.flink.runtime.rest.handler.RestHandlerException;
import org.apache.flink.runtime.rest.handler.legacy.ExecutionGraphCache;
import org.apache.flink.runtime.rest.handler.legacy.metrics.MetricFetcher;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.MessageHeaders;
import org.apache.flink.runtime.rest.messages.ResponseBody;
import org.apache.flink.runtime.rest.messages.job.ExecutionMetricsInfo;
import org.apache.flink.runtime.rest.messages.job.ExecutionMetricsSummaryBuilder;
import org.apache.flink.runtime.rest.messages.job.ExecutionMetricsSummaryInfo;
import org.apache.flink.runtime.rest.messages.job.JobDetailInfo;
import org.apache.flink.runtime.rest.messages.job.JobIDPathParameter;
import org.apache.flink.runtime.rest.messages.job.JobMessageParameters;
import org.apache.flink.runtime.rest.messages.job.VertexSummaryInfo;
import org.apache.flink.runtime.webmonitor.RestfulGateway;
import org.apache.flink.runtime.webmonitor.history.ArchivedJson;
import org.apache.flink.runtime.webmonitor.history.JsonArchivist;
import org.apache.flink.runtime.webmonitor.retriever.GatewayRetriever;
import org.apache.flink.util.Preconditions;

import javax.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Handler returning the details for the specified job.
 */
public class JobDetailHandler extends AbstractExecutionGraphHandler<JobDetailInfo, JobMessageParameters> implements JsonArchivist {

	private final MetricFetcher<?> metricFetcher;

	public JobDetailHandler(
		GatewayRetriever<? extends RestfulGateway> leaderRetriever,
		Time timeout,
		Map<String, String> responseHeaders,
		MessageHeaders<EmptyRequestBody, JobDetailInfo, JobMessageParameters> messageHeaders,
		ExecutionGraphCache executionGraphCache,
		Executor executor,
		MetricFetcher<?> metricFetcher
	) {
		super(
			leaderRetriever,
			timeout,
			responseHeaders,
			messageHeaders,
			executionGraphCache,
			executor
		);

		this.metricFetcher = Preconditions.checkNotNull(metricFetcher);
	}

	@Override
	protected JobDetailInfo handleRequest(
		HandlerRequest<EmptyRequestBody, JobMessageParameters> request,
		AccessExecutionGraph executionGraph
	) throws RestHandlerException {
		return createJobDetailInfo(executionGraph, metricFetcher);
	}

	@Override
	public Collection<ArchivedJson> archiveJsonWithPath(AccessExecutionGraph graph) throws IOException {
		ResponseBody json = createJobDetailInfo(graph, null);
		String path = getMessageHeaders().getTargetRestEndpointURL()
			.replace(':' + JobIDPathParameter.KEY, graph.getJobID().toString());
		return Collections.singleton(new ArchivedJson(path, json));
	}

	private static JobDetailInfo createJobDetailInfo(AccessExecutionGraph executionGraph, @Nullable MetricFetcher<?> metricFetcher) {
		final JobID jobID = executionGraph.getJobID();

		final long startTime = executionGraph.getStatusTimestamp(JobStatus.CREATED);
		final long endTime = executionGraph.getState().isGloballyTerminalState() ?
			executionGraph.getStatusTimestamp(executionGraph.getState()) : -1;
		final long duration = endTime >= 0 ? endTime - startTime : System.currentTimeMillis() - startTime;

		final Map<JobStatus, Long> timestamps = new HashMap<>(JobStatus.values().length);

		for (JobStatus jobStatus : JobStatus.values()) {
			timestamps.put(jobStatus, executionGraph.getStatusTimestamp(jobStatus));
		}

		Collection<VertexSummaryInfo> vertexInfos = new ArrayList<>(executionGraph.getAllVertices().size());
		for (AccessExecutionJobVertex accessExecutionJobVertex : executionGraph.getVerticesTopologically()) {
			VertexSummaryInfo vertexInfo =
				createVertexSummaryInfo(jobID, accessExecutionJobVertex, metricFetcher);
			vertexInfos.add(vertexInfo);
		}

		return new JobDetailInfo(
			executionGraph.getJobID(),
			executionGraph.getJobName(),
			startTime,
			endTime,
			duration,
			executionGraph.getState(),
			executionGraph.getJsonPlan(),
			vertexInfos
		);
	}

	private static VertexSummaryInfo createVertexSummaryInfo(
		JobID jobID,
		AccessExecutionJobVertex ejv,
		MetricFetcher<?> metricFetcher
	) {
		JobVertexID vertexID = ejv.getJobVertexId();

		long startTime = Long.MAX_VALUE;
		long endTime = -1;

		Map<ExecutionState, Integer> numTasksPerState = new HashMap<>(ExecutionState.values().length);
		for (ExecutionState state : ExecutionState.values()) {
			numTasksPerState.put(state, 0);
		}

		ExecutionMetricsSummaryBuilder metricsSummaryBuilder = new ExecutionMetricsSummaryBuilder();

		for (AccessExecutionVertex vertex : ejv.getTaskVertices()) {
			ExecutionState state = vertex.getExecutionState();

			int count = numTasksPerState.get(state);
			numTasksPerState.put(state, count + 1);

			long started = vertex.getStateTimestamp(ExecutionState.CREATED);
			startTime = Math.min(startTime, started);

			long ended = state.isTerminal() ? vertex.getStateTimestamp(state) : -1;
			endTime = Math.max(endTime, ended);

			ExecutionMetricsInfo metrics = ExecutionMetricsUtils.getMetrics(jobID, vertexID, vertex.getCurrentExecutionAttempt(), metricFetcher);
			metricsSummaryBuilder.add(metrics);
		}

		long duration = endTime >= 0 ? endTime - startTime : System.currentTimeMillis() - startTime;

		ExecutionState status =
			ExecutionJobVertex.getVertexState(numTasksPerState, ejv.getParallelism());

		ExecutionMetricsSummaryInfo metricsSummary = metricsSummaryBuilder.build();

		return new VertexSummaryInfo(
			ejv.getJobVertexId(),
			ejv.getTopologyId(),
			ejv.getName(),
			startTime,
			endTime,
			duration,
			ejv.getParallelism(),
			ejv.getMaxParallelism(),
			numTasksPerState,
			status,
			metricsSummary
		);
	}
}
