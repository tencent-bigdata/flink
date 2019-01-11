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

import org.apache.flink.runtime.rest.messages.ConversionException;
import org.apache.flink.runtime.rest.messages.MessagePathParameter;

/**
 * Path parameter specifying the index for a task.
 */
public class TaskIndexPathParameter extends MessagePathParameter<Integer> {

	public static final String KEY = "taskindex";

	public TaskIndexPathParameter() {
		super(KEY);
	}

	@Override
	protected Integer convertFromString(String value) throws ConversionException {
		final int taskIndex = Integer.parseInt(value);
		if (taskIndex >= 0) {
			return taskIndex;
		} else {
			throw new ConversionException("Task index must be positive, was: " + taskIndex);
		}
	}

	@Override
	protected String convertToString(Integer value) {
		return value.toString();
	}

	@Override
	public String getDescription() {
		return "32-character string value that identifies a task.";
	}
}
