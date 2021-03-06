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
package org.apache.flink.runtime.executiongraph;

import org.apache.flink.runtime.accumulators.StringifiedAccumulatorResult;
import org.apache.flink.runtime.execution.ExecutionState;
import org.apache.flink.runtime.jobgraph.JobVertexID;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.apache.flink.runtime.executiongraph.ExecutionJobVertex.getVertexState;

public class ArchivedExecutionJobVertex implements AccessExecutionJobVertex, Serializable {

	private static final long serialVersionUID = -5768187638639437957L;
	private final ArchivedExecutionVertex[] taskVertices;

	private final JobVertexID id;

	private final int topologyId;

	private final String name;

	private final int parallelism;

	private final int maxParallelism;

	private final StringifiedAccumulatorResult[] archivedUserAccumulators;

	public ArchivedExecutionJobVertex(ExecutionJobVertex jobVertex) {
		this.taskVertices = new ArchivedExecutionVertex[jobVertex.getTaskVertices().length];
		for (int x = 0; x < taskVertices.length; x++) {
			taskVertices[x] = jobVertex.getTaskVertices()[x].archive();
		}

		archivedUserAccumulators = jobVertex.getAggregatedUserAccumulatorsStringified();

		this.id = jobVertex.getJobVertexId();
		this.topologyId = jobVertex.getTopologyId();
		this.name = jobVertex.getJobVertex().getName();
		this.parallelism = jobVertex.getParallelism();
		this.maxParallelism = jobVertex.getMaxParallelism();
	}

	public ArchivedExecutionJobVertex(
			ArchivedExecutionVertex[] taskVertices,
			JobVertexID id,
			int topologyId,
			String name,
			int parallelism,
			int maxParallelism,
			StringifiedAccumulatorResult[] archivedUserAccumulators) {
		this.taskVertices = taskVertices;
		this.id = id;
		this.topologyId = topologyId;
		this.name = name;
		this.parallelism = parallelism;
		this.maxParallelism = maxParallelism;
		this.archivedUserAccumulators = archivedUserAccumulators;
	}

	// --------------------------------------------------------------------------------------------
	//   Accessors
	// --------------------------------------------------------------------------------------------

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getParallelism() {
		return parallelism;
	}

	@Override
	public int getMaxParallelism() {
		return maxParallelism;
	}

	@Override
	public JobVertexID getJobVertexId() {
		return id;
	}

	@Override
	public int getTopologyId() {
		return topologyId;
	}

	@Override
	public ArchivedExecutionVertex[] getTaskVertices() {
		return taskVertices;
	}

	@Override
	public ExecutionState getAggregateState() {
		Map<ExecutionState, Integer> numTasksPerState = new HashMap<>(ExecutionState.values().length);
		for (ExecutionState state : ExecutionState.values()) {
			numTasksPerState.put(state, 0);
		}

		for (ArchivedExecutionVertex vertex : this.taskVertices) {
			ExecutionState state = vertex.getExecutionState();
			int count = numTasksPerState.get(state);
			numTasksPerState.put(state, count + 1);
		}

		return getVertexState(numTasksPerState, parallelism);
	}

	// --------------------------------------------------------------------------------------------
	//  Static / pre-assigned input splits
	// --------------------------------------------------------------------------------------------

	@Override
	public StringifiedAccumulatorResult[] getAggregatedUserAccumulatorsStringified() {
		return archivedUserAccumulators;
	}

}
