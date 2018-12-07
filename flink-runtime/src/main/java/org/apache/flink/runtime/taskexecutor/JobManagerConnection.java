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

package org.apache.flink.runtime.taskexecutor;

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.execution.librarycache.LibraryCacheManager;
import org.apache.flink.runtime.jobmaster.JobMasterGateway;
import org.apache.flink.runtime.jobmaster.JobMasterId;
import org.apache.flink.util.Preconditions;

/**
 * Container class for JobManager specific communication utils used by the {@link TaskExecutor}.
 */
public class JobManagerConnection {

	// Job id related with the job manager
	private final JobID jobID;

	// The unique id used for identifying the job manager
	private final ResourceID resourceID;

	// Gateway to the job master
	private final JobMasterGateway jobMasterGateway;

	// Library cache manager connected to the specific job manager
	private final LibraryCacheManager libraryCacheManager;

	public JobManagerConnection(
		JobID jobID,
		ResourceID resourceID,
		JobMasterGateway jobMasterGateway,
		LibraryCacheManager libraryCacheManager
	) {
		this.jobID = Preconditions.checkNotNull(jobID);
		this.resourceID = Preconditions.checkNotNull(resourceID);
		this.jobMasterGateway = Preconditions.checkNotNull(jobMasterGateway);
		this.libraryCacheManager = Preconditions.checkNotNull(libraryCacheManager);
	}

	public JobID getJobID() {
		return jobID;
	}

	public ResourceID getResourceID() {
		return resourceID;
	}

	public JobMasterId getJobMasterId() {
		return jobMasterGateway.getFencingToken();
	}

	public JobMasterGateway getJobManagerGateway() {
		return jobMasterGateway;
	}

	public LibraryCacheManager getLibraryCacheManager() {
		return libraryCacheManager;
	}
}
