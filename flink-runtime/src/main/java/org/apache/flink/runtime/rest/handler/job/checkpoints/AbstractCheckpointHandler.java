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
import org.apache.flink.runtime.executiongraph.AccessExecutionGraph;
import org.apache.flink.runtime.rest.handler.HandlerRequest;
import org.apache.flink.runtime.rest.handler.RestHandlerException;
import org.apache.flink.runtime.rest.handler.job.AbstractExecutionGraphHandler;
import org.apache.flink.runtime.rest.handler.legacy.ExecutionGraphCache;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.MessageHeaders;
import org.apache.flink.runtime.rest.messages.ResponseBody;
import org.apache.flink.runtime.rest.messages.job.checkpoints.CheckpointIDPathParameter;
import org.apache.flink.runtime.rest.messages.job.checkpoints.CheckpointMessageParameters;
import org.apache.flink.runtime.webmonitor.RestfulGateway;
import org.apache.flink.runtime.webmonitor.retriever.GatewayRetriever;

import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Base class for checkpoint related REST handler.
 *
 * @param <R> type of the response
 */
public abstract class AbstractCheckpointHandler<R extends ResponseBody, M extends CheckpointMessageParameters> extends AbstractExecutionGraphHandler<R, M> {

	protected AbstractCheckpointHandler(
		GatewayRetriever<? extends RestfulGateway> leaderRetriever,
		Time timeout,
		Map<String, String> responseHeaders,
		MessageHeaders<EmptyRequestBody, R, M> messageHeaders,
		ExecutionGraphCache executionGraphCache,
		Executor executor
	) {
		super(leaderRetriever, timeout, responseHeaders, messageHeaders, executionGraphCache, executor);
	}

	@Override
	protected R handleRequest(HandlerRequest<EmptyRequestBody, M> request, AccessExecutionGraph executionGraph) throws RestHandlerException {
		final long checkpointId = request.getPathParameter(CheckpointIDPathParameter.class);

		final CheckpointTracesSnapshot checkpointTracesSnapshot = executionGraph.getCheckpointTracesSnapshot();

		if (checkpointTracesSnapshot != null) {
			CheckpointTrace checkpointTrace = checkpointTracesSnapshot.getCheckpointTrace(checkpointId);
			if (checkpointTrace != null) {
				return handleCheckpointRequest(request, checkpointTrace);
			} else {
				throw new RestHandlerException("Could not find checkpointing statistics for checkpoint " + checkpointId + '.', HttpResponseStatus.NOT_FOUND);
			}
		} else {
			throw new RestHandlerException("Checkpointing was not enabled for job " + executionGraph.getJobID() + '.', HttpResponseStatus.NOT_FOUND);
		}
	}

	/**
	 * Called for each request with the corresponding {@link CheckpointTrace} instance.
	 *
	 * @param request for further information
	 * @param checkpointTrace for which the handler is called
	 * @return Response
	 * @throws RestHandlerException if the handler could not handle the request
	 */
	protected abstract R handleCheckpointRequest(HandlerRequest<EmptyRequestBody, M> request, CheckpointTrace checkpointTrace) throws RestHandlerException;
}
