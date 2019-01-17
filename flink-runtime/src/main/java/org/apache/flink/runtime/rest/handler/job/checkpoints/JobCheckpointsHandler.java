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

package org.apache.flink.runtime.rest.handler.job.checkpoints;

import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.checkpoint.CheckpointTrace;
import org.apache.flink.runtime.checkpoint.CheckpointTracesSnapshot;
import org.apache.flink.runtime.checkpoint.VertexCheckpointTrace;
import org.apache.flink.runtime.executiongraph.AccessExecutionGraph;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.rest.handler.HandlerRequest;
import org.apache.flink.runtime.rest.handler.RestHandlerException;
import org.apache.flink.runtime.rest.handler.job.AbstractExecutionGraphHandler;
import org.apache.flink.runtime.rest.handler.legacy.ExecutionGraphCache;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.ErrorResponseBody;
import org.apache.flink.runtime.rest.messages.MessageHeaders;
import org.apache.flink.runtime.rest.messages.ResponseBody;
import org.apache.flink.runtime.rest.messages.job.JobIDPathParameter;
import org.apache.flink.runtime.rest.messages.job.JobMessageParameters;
import org.apache.flink.runtime.rest.messages.job.checkpoints.CheckpointSummaryInfo;
import org.apache.flink.runtime.rest.messages.job.checkpoints.JobCheckpointsInfo;
import org.apache.flink.runtime.webmonitor.RestfulGateway;
import org.apache.flink.runtime.webmonitor.history.ArchivedJson;
import org.apache.flink.runtime.webmonitor.history.JsonArchivist;
import org.apache.flink.runtime.webmonitor.retriever.GatewayRetriever;

import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * REST handler which returns the overview over the checkpoints for a checkpoint.
 */
public class JobCheckpointsHandler extends AbstractExecutionGraphHandler<JobCheckpointsInfo, JobMessageParameters> implements JsonArchivist {

	public JobCheckpointsHandler(
			GatewayRetriever<? extends RestfulGateway> leaderRetriever,
			Time timeout,
			Map<String, String> responseHeaders,
			MessageHeaders<EmptyRequestBody, JobCheckpointsInfo, JobMessageParameters> messageHeaders,
			ExecutionGraphCache executionGraphCache,
			Executor executor) {
		super(leaderRetriever, timeout, responseHeaders, messageHeaders, executionGraphCache, executor);
	}

	@Override
	protected JobCheckpointsInfo handleRequest(HandlerRequest<EmptyRequestBody, JobMessageParameters> request, AccessExecutionGraph executionGraph) throws RestHandlerException {
		return createCheckpointsOverviewInfo(executionGraph);
	}

	@Override
	public Collection<ArchivedJson> archiveJsonWithPath(AccessExecutionGraph graph) throws IOException {
		ResponseBody json;
		try {
			json = createCheckpointsOverviewInfo(graph);
		} catch (RestHandlerException rhe) {
			json = new ErrorResponseBody(rhe.getMessage());
		}
		String path = getMessageHeaders().getTargetRestEndpointURL()
			.replace(':' + JobIDPathParameter.KEY, graph.getJobID().toString());
		return Collections.singletonList(new ArchivedJson(path, json));
	}

	private static JobCheckpointsInfo createCheckpointsOverviewInfo(AccessExecutionGraph executionGraph) throws RestHandlerException {
		final CheckpointTracesSnapshot checkpointTracesSnapshot = executionGraph.getCheckpointTracesSnapshot();

		if (checkpointTracesSnapshot == null) {
			throw new RestHandlerException("Checkpointing has not been enabled.", HttpResponseStatus.NOT_FOUND);
		} else {

			CheckpointTrace lastCompletedCheckpointTrace = checkpointTracesSnapshot.getLastCompletedCheckpointTrace();
			CheckpointTrace lastFailedCheckpointTrace = checkpointTracesSnapshot.getLastFailedCheckpointTrace();
			Map<Long, CheckpointTrace> checkpointTraces = checkpointTracesSnapshot.getCheckpointTraces();

			CheckpointSummaryInfo lastCompletedCheckpointSummary = createCheckpointSummaryInfo(lastCompletedCheckpointTrace);
			CheckpointSummaryInfo lastFailedCheckpointSummary = createCheckpointSummaryInfo(lastFailedCheckpointTrace);
			Collection<CheckpointSummaryInfo> checkpointSummaries = checkpointTraces.values().stream()
				.map(JobCheckpointsHandler::createCheckpointSummaryInfo)
				.collect(Collectors.toList());

			return new JobCheckpointsInfo(
				checkpointTracesSnapshot.getNumPendingCheckpoints(),
				checkpointTracesSnapshot.getNumCompletedCheckpoints(),
				checkpointTracesSnapshot.getNumFailedCheckpoints(),
				lastCompletedCheckpointSummary,
				lastFailedCheckpointSummary,
				checkpointSummaries
			);
		}
	}

	private static CheckpointSummaryInfo createCheckpointSummaryInfo(CheckpointTrace checkpointTrace) {

		if (checkpointTrace == null) {
			return null;
		}

		int numTasks = 0;
		int numAcknowledgedTasks = 0;
		long size = 0;

		Map<JobVertexID, VertexCheckpointTrace> vertexTraces = checkpointTrace.getVertexTraces();
		for (VertexCheckpointTrace vertexTrace : vertexTraces.values()) {
			numTasks += vertexTrace.getNumTasks();
			numAcknowledgedTasks += vertexTrace.getNumAcknowledgedTasks();
			size += vertexTrace.getSize();
		}

		return new CheckpointSummaryInfo(
			checkpointTrace.getCheckpointId(),
			checkpointTrace.getCheckpointType(),
			checkpointTrace.getCheckpointStatus(),
			checkpointTrace.getStartTime(),
			checkpointTrace.getEndTime(),
			checkpointTrace.getDuration(),
			numTasks,
			numAcknowledgedTasks,
			size,
			checkpointTrace.getFailure()
		);
	}
}
