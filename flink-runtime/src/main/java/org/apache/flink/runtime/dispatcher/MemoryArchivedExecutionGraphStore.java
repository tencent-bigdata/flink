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

package org.apache.flink.runtime.dispatcher;

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.executiongraph.AccessExecutionGraph;
import org.apache.flink.runtime.executiongraph.ArchivedExecutionGraph;
import org.apache.flink.runtime.rest.messages.job.JobSummaryInfo;
import org.apache.flink.runtime.rest.messages.job.JobsOverviewInfo;

import javax.annotation.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link ArchivedExecutionGraphStore} implementation which stores the {@link ArchivedExecutionGraph}
 * in memory.
 */
public class MemoryArchivedExecutionGraphStore implements ArchivedExecutionGraphStore {

	private final Map<JobID, ArchivedExecutionGraph> serializableExecutionGraphs = new HashMap<>(4);

	@Override
	public int size() {
		return serializableExecutionGraphs.size();
	}

	@Nullable
	@Override
	public ArchivedExecutionGraph get(JobID jobId) {
		return serializableExecutionGraphs.get(jobId);
	}

	@Override
	public void put(ArchivedExecutionGraph serializableExecutionGraph) throws IOException {
		serializableExecutionGraphs.put(serializableExecutionGraph.getJobID(), serializableExecutionGraph);
	}

	@Override
	public JobsOverviewInfo getStoredJobsOverview() {
		Collection<JobSummaryInfo> jobSummaries = serializableExecutionGraphs.values().stream()
			.map(AccessExecutionGraph::getJobSummary)
			.collect(Collectors.toList());

		return JobsOverviewInfo.create(jobSummaries);
	}

	@Nullable
	@Override
	public JobSummaryInfo getAvailableJobSummary(JobID jobId) {
		final ArchivedExecutionGraph archivedExecutionGraph = serializableExecutionGraphs.get(jobId);

		if (archivedExecutionGraph != null) {
			return archivedExecutionGraph.getJobSummary();
		} else {
			return null;
		}
	}

	@Override
	public void close() throws IOException {
		serializableExecutionGraphs.clear();
	}
}
