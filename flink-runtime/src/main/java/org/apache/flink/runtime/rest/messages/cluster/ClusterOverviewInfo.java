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

package org.apache.flink.runtime.rest.messages.cluster;

import org.apache.flink.runtime.rest.messages.ResponseBody;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * The information for the clusters.
 */
public class ClusterOverviewInfo implements ResponseBody {

	private static final String FIELD_NAME_VERSION = "version";
	private static final String FIELD_NAME_COMMIT = "commit";

	@JsonProperty(FIELD_NAME_VERSION)
	private final String version;

	@JsonProperty(FIELD_NAME_COMMIT)
	private final String commit;

	@JsonCreator
	public ClusterOverviewInfo(
		@JsonProperty(FIELD_NAME_VERSION) String version,
		@JsonProperty(FIELD_NAME_COMMIT) String commit
	) {
		this.version = version;
		this.commit = commit;
	}

	public String getVersion() {
		return version;
	}

	public String getCommit() {
		return commit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ClusterOverviewInfo that = (ClusterOverviewInfo) o;
		return Objects.equals(version, that.version) &&
			Objects.equals(commit, that.commit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(version, commit);
	}

	@Override
	public String toString() {
		return "ClusterInfo{" +
			"version='" + version + '\'' +
			", commit='" + commit + '\'' +
			'}';
	}
}
