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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * The snapshot for failure traces.
 */
public class ExceptionTracesSnapshot implements Serializable {

	private final int numFailures;

	private final Collection<ExceptionTrace> exceptionTraces;

	public ExceptionTracesSnapshot(
		int numFailures,
		Collection<ExceptionTrace> exceptionTraces
	) {
		this.numFailures = numFailures;
		this.exceptionTraces = exceptionTraces;
	}

	public ExceptionTracesSnapshot() {
		this(0, Collections.emptyList());
	}

	public int getNumFailures() {
		return numFailures;
	}

	public Collection<ExceptionTrace> getExceptionTraces() {
		return exceptionTraces;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ExceptionTracesSnapshot that = (ExceptionTracesSnapshot) o;
		return numFailures == that.numFailures &&
			Objects.equals(exceptionTraces, that.exceptionTraces);
	}

	@Override
	public int hashCode() {
		return Objects.hash(numFailures, exceptionTraces);
	}

	@Override
	public String toString() {
		return "ExceptionTracesSnapshot{" +
			"numFailures=" + numFailures +
			", exceptionTraces=" + exceptionTraces +
			'}';
	}
}
