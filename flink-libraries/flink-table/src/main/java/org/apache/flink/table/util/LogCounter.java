package org.apache.flink.table.util;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Log counter.
 */
public class LogCounter implements Serializable {

	private AtomicInteger counter = new AtomicInteger(0);

	private int start = 10;
	private int control = 1000;
	private int reset = 60 * 1000;

	private long lastLogTime = System.currentTimeMillis();

	public LogCounter(int start, int control, int reset) {
		this.start = start;
		this.control = control;
		this.reset = reset;
	}

	public boolean shouldPrint(){
		if (System.currentTimeMillis() - lastLogTime > reset) {
			counter.set(0);
			this.lastLogTime = System.currentTimeMillis();
		}

		if (counter.incrementAndGet() > start && counter.get() % control != 0) {
			return false;
		}

		return true;
	}
}
