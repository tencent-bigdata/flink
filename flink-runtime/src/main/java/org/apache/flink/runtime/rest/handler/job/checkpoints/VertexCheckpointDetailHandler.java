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
import org.apache.flink.runtime.rest.NotFoundException;
import org.apache.flink.runtime.rest.handler.HandlerRequest;
import org.apache.flink.runtime.rest.handler.RestHandlerException;
import org.apache.flink.runtime.rest.handler.legacy.ExecutionGraphCache;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.MessageHeaders;
import org.apache.flink.runtime.rest.messages.ResponseBody;
import org.apache.flink.runtime.rest.messages.job.JobIDPathParameter;
import org.apache.flink.runtime.rest.messages.job.VertexIDPathParameter;
import org.apache.flink.runtime.rest.messages.job.checkpoints.CheckpointIDPathParameter;
import org.apache.flink.runtime.rest.messages.job.checkpoints.CheckpointMetricsInfo;
import org.apache.flink.runtime.rest.messages.job.checkpoints.TaskCheckpointInfo;
import org.apache.flink.runtime.rest.messages.job.checkpoints.VertexCheckpointDetailInfo;
import org.apache.flink.runtime.rest.messages.job.checkpoints.VertexCheckpointMessageParameters;
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
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * REST handler which serves checkpoint statistics for subtasks.
 */
public class VertexCheckpointDetailHandler
	extends AbstractCheckpointHandler<VertexCheckpointDetailInfo, VertexCheckpointMessageParameters>
	implements JsonArchivist {

	public VertexCheckpointDetailHandler(
		GatewayRetriever<? extends RestfulGateway> leaderRetriever,
		Time timeout,
		Map<String, String> responseHeaders,
		MessageHeaders<EmptyRequestBody, VertexCheckpointDetailInfo, VertexCheckpointMessageParameters> messageHeaders,
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
	protected VertexCheckpointDetailInfo handleCheckpointRequest(
		HandlerRequest<EmptyRequestBody, VertexCheckpointMessageParameters> request,
		CheckpointTrace checkpointTrace
	) throws RestHandlerException {

		final JobVertexID vertexId = request.getPathParameter(VertexIDPathParameter.class);
		final VertexCheckpointTrace vertexTrace = checkpointTrace.getVertexTrace(vertexId);
		if (vertexTrace == null) {
			throw new NotFoundException(
				"There is no checkpoint trace for vertex " + vertexId + '.');
		}

		return createVertexCheckpointDetailInfo(vertexId, vertexTrace);
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
			Map<JobVertexID, VertexCheckpointTrace> vertexTraces = checkpointTrace.getVertexTraces();
			for (Map.Entry<JobVertexID, VertexCheckpointTrace> vertexEntry : vertexTraces.entrySet()) {
				JobVertexID vertexId = vertexEntry.getKey();
				VertexCheckpointTrace vertexTrace = vertexEntry.getValue();

				ResponseBody json = createVertexCheckpointDetailInfo(vertexId, vertexTrace);
				String path = getMessageHeaders().getTargetRestEndpointURL()
					.replace(':' + JobIDPathParameter.KEY, graph.getJobID().toString())
					.replace(':' + CheckpointIDPathParameter.KEY, String.valueOf(checkpointTrace.getCheckpointId()))
					.replace(':' + VertexIDPathParameter.KEY, vertexId.toString());
				archive.add(new ArchivedJson(path, json));
			}
		}
		return archive;
	}

	private static VertexCheckpointDetailInfo createVertexCheckpointDetailInfo(
		JobVertexID vertexId,
		VertexCheckpointTrace vertexTrace
	) {

		Collection<TaskCheckpointInfo> taskInfos = new ArrayList<>();

		Map<Integer, TaskCheckpointTrace> taskTraces = vertexTrace.getAcknowledgedTaskTraces();
		for (Map.Entry<Integer, TaskCheckpointTrace> taskEntry : taskTraces.entrySet()) {
			int taskIndex = taskEntry.getKey();
			TaskCheckpointTrace taskTrace = taskEntry.getValue();
			TaskCheckpointInfo taskInfo = createTaskCheckpointInfo(taskIndex, taskTrace);
			taskInfos.add(taskInfo);
		}

		Set<Integer> unacknowledgedTasks = vertexTrace.getUnacknowledgedTasks();
		for (int taskIndex : unacknowledgedTasks) {
			TaskCheckpointInfo taskInfo = new TaskCheckpointInfo(taskIndex);
			taskInfos.add(taskInfo);
		}

		return new VertexCheckpointDetailInfo(
			vertexId,
			vertexTrace.getNumTasks(),
			vertexTrace.getNumAcknowledgedTasks(),
			taskInfos
		);
	}

	private static TaskCheckpointInfo createTaskCheckpointInfo(
		int taskIndex,
		TaskCheckpointTrace taskTrace
	) {
		return new TaskCheckpointInfo(
			taskIndex,
			taskTrace.getStartInstant(),
			taskTrace.getEndInstant(),
			new CheckpointMetricsInfo(
				taskTrace.getAlignDuration(),
				taskTrace.getSyncDuration(),
				taskTrace.getAsyncDuration(),
				taskTrace.getDuration(),
				taskTrace.getSize()
			)
		);
	}
}
