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

import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.executiongraph.AccessExecutionGraph;
import org.apache.flink.runtime.executiongraph.ExceptionTrace;
import org.apache.flink.runtime.executiongraph.ExceptionTracesSnapshot;
import org.apache.flink.runtime.rest.handler.HandlerRequest;
import org.apache.flink.runtime.rest.handler.legacy.ExecutionGraphCache;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.MessageHeaders;
import org.apache.flink.runtime.rest.messages.ResponseBody;
import org.apache.flink.runtime.rest.messages.job.ExceptionInfo;
import org.apache.flink.runtime.rest.messages.job.ExecutorInfo;
import org.apache.flink.runtime.rest.messages.job.JobExceptionsInfo;
import org.apache.flink.runtime.rest.messages.job.JobIDPathParameter;
import org.apache.flink.runtime.rest.messages.job.JobMessageParameters;
import org.apache.flink.runtime.webmonitor.RestfulGateway;
import org.apache.flink.runtime.webmonitor.history.ArchivedJson;
import org.apache.flink.runtime.webmonitor.history.JsonArchivist;
import org.apache.flink.runtime.webmonitor.retriever.GatewayRetriever;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Handler serving the job exceptions.
 */
public class JobExceptionsHandler extends AbstractExecutionGraphHandler<JobExceptionsInfo, JobMessageParameters> implements JsonArchivist {

	public JobExceptionsHandler(
			GatewayRetriever<? extends RestfulGateway> leaderRetriever,
			Time timeout,
			Map<String, String> responseHeaders,
			MessageHeaders<EmptyRequestBody, JobExceptionsInfo, JobMessageParameters> messageHeaders,
			ExecutionGraphCache executionGraphCache,
			Executor executor) {

		super(
			leaderRetriever,
			timeout,
			responseHeaders,
			messageHeaders,
			executionGraphCache,
			executor);
	}

	@Override
	protected JobExceptionsInfo handleRequest(HandlerRequest<EmptyRequestBody, JobMessageParameters> request, AccessExecutionGraph executionGraph) {
		return createJobExceptionsInfo(executionGraph);
	}

	@Override
	public Collection<ArchivedJson> archiveJsonWithPath(AccessExecutionGraph graph) throws IOException {
		ResponseBody json = createJobExceptionsInfo(graph);
		String path = getMessageHeaders().getTargetRestEndpointURL()
			.replace(':' + JobIDPathParameter.KEY, graph.getJobID().toString());
		return Collections.singletonList(new ArchivedJson(path, json));
	}

	private static JobExceptionsInfo createJobExceptionsInfo(AccessExecutionGraph executionGraph) {
		ExceptionTracesSnapshot exceptionTracesSnapshot = executionGraph.getExceptionTracesSnapshot();

		int numExceptions = exceptionTracesSnapshot.getNumFailures();
		Collection<ExceptionInfo> execeptionInfos = exceptionTracesSnapshot.getExceptionTraces().stream()
			.map(failureTrace -> createExceptionInfo(failureTrace))
			.collect(Collectors.toList());

		return new JobExceptionsInfo(numExceptions, execeptionInfos);
	}

	private static ExceptionInfo createExceptionInfo(ExceptionTrace exceptionTrace) {
		ExecutorInfo executorInfo =
			exceptionTrace.getTaskManagerLocation() == null ? null :
				new ExecutorInfo(
					exceptionTrace.getTaskManagerLocation().getResourceID(),
					exceptionTrace.getTaskManagerLocation().getFQDNHostname(),
					exceptionTrace.getTaskManagerLocation().dataPort()
				);

		return new ExceptionInfo(
			exceptionTrace.getFailInstant(),
			exceptionTrace.getFailCause(),
			exceptionTrace.getExecutionId(),
			exceptionTrace.getTaskName(),
			exceptionTrace.getAttemptNumber(),
			executorInfo
		);
	}
}
