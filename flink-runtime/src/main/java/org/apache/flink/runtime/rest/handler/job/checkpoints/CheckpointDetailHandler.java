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
import org.apache.flink.runtime.checkpoint.TaskCheckpointTrace;
import org.apache.flink.runtime.checkpoint.VertexCheckpointTrace;
import org.apache.flink.runtime.executiongraph.AccessExecutionGraph;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.rest.handler.HandlerRequest;
import org.apache.flink.runtime.rest.handler.legacy.ExecutionGraphCache;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.MessageHeaders;
import org.apache.flink.runtime.rest.messages.ResponseBody;
import org.apache.flink.runtime.rest.messages.job.JobIDPathParameter;
import org.apache.flink.runtime.rest.messages.job.checkpoints.CheckpointDetailInfo;
import org.apache.flink.runtime.rest.messages.job.checkpoints.CheckpointIDPathParameter;
import org.apache.flink.runtime.rest.messages.job.checkpoints.CheckpointMessageParameters;
import org.apache.flink.runtime.rest.messages.job.checkpoints.CheckpointMetricsInfo;
import org.apache.flink.runtime.rest.messages.job.checkpoints.CheckpointMetricsSummaryBuilder;
import org.apache.flink.runtime.rest.messages.job.checkpoints.VertexCheckpointSummaryInfo;
import org.apache.flink.runtime.webmonitor.RestfulGateway;
import org.apache.flink.runtime.webmonitor.history.ArchivedJson;
import org.apache.flink.runtime.webmonitor.history.JsonArchivist;
import org.apache.flink.runtime.webmonitor.retriever.GatewayRetriever;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * REST handler which returns the details for a checkpoint.
 */
public class CheckpointDetailHandler extends AbstractCheckpointHandler<CheckpointDetailInfo, CheckpointMessageParameters> implements JsonArchivist {

	public CheckpointDetailHandler(
		GatewayRetriever<? extends RestfulGateway> leaderRetriever,
		Time timeout,
		Map<String, String> responseHeaders,
		MessageHeaders<EmptyRequestBody, CheckpointDetailInfo, CheckpointMessageParameters> messageHeaders,
		ExecutionGraphCache executionGraphCache,
		Executor executor
	) {
		super(
			leaderRetriever,
			timeout,
			responseHeaders,
			messageHeaders,
			executionGraphCache,
			executor
		);
	}

	@Override
	protected CheckpointDetailInfo handleCheckpointRequest(HandlerRequest<EmptyRequestBody, CheckpointMessageParameters> ignored, CheckpointTrace checkpointTrace) {
		return createCheckpointDetailInfo(checkpointTrace);
	}

	@Override
	public Collection<ArchivedJson> archiveJsonWithPath(AccessExecutionGraph graph) throws IOException {
		CheckpointTracesSnapshot checkpointTracesSnapshot = graph.getCheckpointTracesSnapshot();
		if (checkpointTracesSnapshot == null) {
			return Collections.emptyList();
		}

		Map<Long, CheckpointTrace> checkpointTraces = checkpointTracesSnapshot.getCheckpointTraces();
		List<ArchivedJson> archive = new ArrayList<>(checkpointTraces.size());
		for (CheckpointTrace checkpointTrace : checkpointTraces.values()) {
			ResponseBody json = createCheckpointDetailInfo(checkpointTrace);
			String path = getMessageHeaders().getTargetRestEndpointURL()
				.replace(':' + JobIDPathParameter.KEY, graph.getJobID().toString())
				.replace(':' + CheckpointIDPathParameter.KEY, String.valueOf(checkpointTrace.getCheckpointId()));
			archive.add(new ArchivedJson(path, json));
		}
		return archive;
	}

	private static CheckpointDetailInfo createCheckpointDetailInfo(CheckpointTrace checkpointTrace) {

		int numTasks = 0;
		int numAcknowledgedTasks = 0;
		long size = 0;
		Collection<VertexCheckpointSummaryInfo> vertexCheckpointDetails = new ArrayList<>();

		Map<JobVertexID, VertexCheckpointTrace> vertexCheckpointTraces = checkpointTrace.getVertexTraces();
		for (Map.Entry<JobVertexID, VertexCheckpointTrace> vertexEntry : vertexCheckpointTraces.entrySet()) {
			JobVertexID vertexId = vertexEntry.getKey();
			VertexCheckpointTrace vertexTrace = vertexEntry.getValue();

			numTasks += vertexTrace.getNumTasks();
			numAcknowledgedTasks += vertexTrace.getNumAcknowledgedTasks();

			CheckpointMetricsSummaryBuilder metricsSummaryBuilder = new CheckpointMetricsSummaryBuilder();
			for (TaskCheckpointTrace taskTrace : vertexTrace.getAcknowledgedTaskTraces().values()) {
				CheckpointMetricsInfo metrics =
					new CheckpointMetricsInfo(
						taskTrace.getAlignDuration(),
						taskTrace.getSyncDuration(),
						taskTrace.getAsyncDuration(),
						taskTrace.getDuration(),
						taskTrace.getSize()
					);
				metricsSummaryBuilder.add(metrics);

				size += taskTrace.getSize();
			}

			VertexCheckpointSummaryInfo vertexCheckpointDetail =
				new VertexCheckpointSummaryInfo(
					vertexId,
					vertexTrace.getNumTasks(),
					vertexTrace.getNumAcknowledgedTasks(),
					metricsSummaryBuilder.build()
				);
			vertexCheckpointDetails.add(vertexCheckpointDetail);
		}

		return new CheckpointDetailInfo(
			checkpointTrace.getCheckpointId(),
			checkpointTrace.getCheckpointType(),
			checkpointTrace.getCheckpointStatus(),
			checkpointTrace.getStartTime(),
			checkpointTrace.getEndTime(),
			checkpointTrace.getDuration(),
			numTasks,
			numAcknowledgedTasks,
			size,
			checkpointTrace.getFailure(),
			vertexCheckpointDetails
		);

	}
}
