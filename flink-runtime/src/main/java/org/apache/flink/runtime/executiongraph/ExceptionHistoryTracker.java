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

import org.apache.flink.util.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * The tracker for failures.
 */
public class ExceptionHistoryTracker {

	private final int numRememberedExceptions;

	private volatile int numExceptions;

	private final PriorityQueue<ExceptionTrace> exceptionTraces;

	public ExceptionHistoryTracker(int numRememberedExceptions) {
		this.numRememberedExceptions = numRememberedExceptions;
		this.numExceptions = 0;
		this.exceptionTraces = new PriorityQueue<>(Comparator.comparingLong(ExceptionTrace::getFailInstant));
	}

	public void addFailureTrace(ExceptionTrace trace) {
		Preconditions.checkNotNull(trace);

		numExceptions++;

		exceptionTraces.add(trace);
		if (exceptionTraces.size() > numRememberedExceptions) {
			exceptionTraces.poll();
		}
	}

	public ExceptionTracesSnapshot snapshot() {
		Collection<ExceptionTrace> exceptionTracesCopy = new ArrayList<>(exceptionTraces);
		return new ExceptionTracesSnapshot(numExceptions, exceptionTracesCopy);
	}
}
