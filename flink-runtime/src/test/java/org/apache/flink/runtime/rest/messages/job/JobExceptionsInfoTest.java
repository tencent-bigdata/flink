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

package org.apache.flink.runtime.rest.messages.job;

import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.executiongraph.ExecutionAttemptID;
import org.apache.flink.runtime.rest.messages.RestResponseMarshallingTestBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Tests that the {@link JobExceptionsInfo} can be marshalled and unmarshalled.
 */
public class JobExceptionsInfoTest extends RestResponseMarshallingTestBase<JobExceptionsInfo> {
	@Override
	protected Class<JobExceptionsInfo> getTestResponseClass() {
		return JobExceptionsInfo.class;
	}

	@Override
	protected JobExceptionsInfo getTestResponseInstance() throws Exception {
		Random random = new Random();

		int numExceptions = random.nextInt(20);
		Collection<ExceptionInfo> exceptionInfos = new ArrayList<>(numExceptions);
		for (int i = 0; i < numExceptions; ++i) {
			exceptionInfos.add(createRandomExceptionInfo(random));
		}

		return new JobExceptionsInfo(numExceptions, exceptionInfos);
	}

	private static ExceptionInfo createRandomExceptionInfo(Random random) {
		return new ExceptionInfo(
			random.nextLong(),
			"failure" + random.nextInt(),
			new ExecutionAttemptID(),
			"task" + random.nextInt(),
			random.nextInt(),
			new ExecutorInfo(ResourceID.generate(), "host" + random.nextInt(), random.nextInt())
		);
	}
}
