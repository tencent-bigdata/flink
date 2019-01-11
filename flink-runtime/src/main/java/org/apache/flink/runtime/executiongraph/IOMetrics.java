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

import org.apache.flink.metrics.Meter;

import java.io.Serializable;

/**
 * An instance of this class represents a snapshot of the io-related metrics of a single task.
 */
public class IOMetrics implements Serializable {

	private static final long serialVersionUID = -7208093607556457183L;

	protected long numRecordsIn;
	protected long numRecordsOut;

	protected double rateRecordsIn;
	protected double rateRecordsOut;

	protected long numBytesInLocal;
	protected long numBytesInRemote;
	protected long numBytesOut;

	protected double rateBytesInLocal;
	protected double rateBytesInRemote;
	protected double rateBytesOut;

	public IOMetrics(
		Meter recordsIn,
		Meter recordsOut,
		Meter bytesLocalIn,
		Meter bytesRemoteIn,
		Meter bytesOut
	) {
		this.numRecordsIn = recordsIn.getCount();
		this.rateRecordsIn = recordsIn.getRate();
		this.numRecordsOut = recordsOut.getCount();
		this.rateRecordsOut = recordsOut.getRate();
		this.numBytesInLocal = bytesLocalIn.getCount();
		this.rateBytesInLocal = bytesLocalIn.getRate();
		this.numBytesInRemote = bytesRemoteIn.getCount();
		this.rateBytesInRemote = bytesRemoteIn.getRate();
		this.numBytesOut = bytesOut.getCount();
		this.rateBytesOut = bytesOut.getRate();
	}

	public IOMetrics(
		long numBytesInLocal,
		long numBytesInRemote,
		long numBytesOut,
		long numRecordsIn,
		long numRecordsOut,
		double rateBytesInLocal,
		double rateBytesInRemote,
		double rateBytesOut,
		double rateRecordsIn,
		double rateRecordsOut
	) {
		this.numBytesInLocal = numBytesInLocal;
		this.numBytesInRemote = numBytesInRemote;
		this.numBytesOut = numBytesOut;
		this.numRecordsIn = numRecordsIn;
		this.numRecordsOut = numRecordsOut;
		this.rateBytesInLocal = rateBytesInLocal;
		this.rateBytesInRemote = rateBytesInRemote;
		this.rateBytesOut = rateBytesOut;
		this.rateRecordsIn = rateRecordsIn;
		this.rateRecordsOut = rateRecordsOut;
	}

	public long getNumRecordsIn() {
		return numRecordsIn;
	}

	public long getNumRecordsOut() {
		return numRecordsOut;
	}

	public long getNumBytesInLocal() {
		return numBytesInLocal;
	}

	public long getNumBytesInRemote() {
		return numBytesInRemote;
	}

	public long getNumBytesIn() {
		return numBytesInLocal + numBytesInRemote;
	}

	public long getNumBytesOut() {
		return numBytesOut;
	}

	public double getRateRecordsIn() {
		return rateRecordsIn;
	}

	public double getRateRecordsOut() {
		return rateRecordsOut;
	}

	public double getRateBytesInLocal() {
		return rateBytesInLocal;
	}

	public double getRateBytesInRemote() {
		return rateBytesInRemote;
	}

	public double getRateBytesIn() {
		return rateBytesInLocal + rateBytesInRemote;
	}

	public double getRateBytesOut() {
		return rateBytesOut;
	}
}
