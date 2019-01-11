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
import org.apache.flink.runtime.rest.messages.json.ResourceIDDeserializer;
import org.apache.flink.runtime.rest.messages.json.ResourceIDSerializer;
import org.apache.flink.util.Preconditions;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

/**
 * The information for the TaskManager executing the task.
 */
public class ExecutorInfo {

	private static final String FIELD_NAME_ID = "id";
	private static final String FIELD_NAME_HOST = "host";
	private static final String FIELD_NAME_PORT = "port";

	@JsonProperty(FIELD_NAME_ID)
	@JsonSerialize(using = ResourceIDSerializer.class)
	private final ResourceID id;

	@JsonProperty(FIELD_NAME_HOST)
	private final String host;

	@JsonProperty(FIELD_NAME_PORT)
	private final int port;

	@JsonCreator
	public ExecutorInfo(
		@JsonProperty(FIELD_NAME_ID) @JsonDeserialize(using = ResourceIDDeserializer.class) ResourceID id,
		@JsonProperty(FIELD_NAME_HOST) String host,
		@JsonProperty(FIELD_NAME_PORT) int port
	) {
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(host);

		this.id = id;
		this.host = host;
		this.port = port;
	}

	public ResourceID getId() {
		return id;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ExecutorInfo that = (ExecutorInfo) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(host, that.host) &&
			Objects.equals(port, that.port);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, host, port);
	}

	@Override
	public String toString() {
		return "ExecutorInfo{" +
			"id=" + id +
			", host='" + host + '\'' +
			", port=" + port +
			'}';
	}
}
