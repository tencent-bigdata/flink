/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.streaming.api.windowing.assigners;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.CustomEventTimeTrigger;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.windows.CustomTimeWindow;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Custom Tumbling Event Time Windows.
 */
public class CustomTumblingEventTimeWindows extends WindowAssigner<Object, CustomTimeWindow> {

	private static final Logger LOG = LoggerFactory.getLogger(CustomTumblingEventTimeWindows.class);
	private static final long serialVersionUID = 1L;

	private final long size;

	private Map<Long, Long> windowtimouMap = new TreeMap<>();

	private long windowWaitTs = 1000; // 窗口额外等待时间，防止还有该窗口数据到达
	private int windowtimoutLimit = 5000;

	public CustomTumblingEventTimeWindows(long size, long offset) {

		if (offset < 0 || offset >= size) {
			throw new IllegalArgumentException(
				"CustomTumblingEventTimeWindows parameters must satisfy 0 <= offset < size");
		}
		this.size = size;
		LOG.info("CustomTumblingEventTimeWindows has been invoked");
	}

	@Override
	public Collection<CustomTimeWindow> assignWindows(Object element, long timestamp, org.apache.flink.streaming.api.windowing.assigners.WindowAssigner.WindowAssignerContext context) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(CustomTumblingEventTimeWindows.class.getCanonicalName() + " assignWindows has been invoked");
		}

		if (timestamp > Long.MIN_VALUE) {
			long now = System.currentTimeMillis();
			// Long.MIN_VALUE is currently assigned when no timestamp is present

			long winStart = TimeWindow.getWindowStart(timestamp, size);
			long windowTimeout = 0;
			long end = winStart + size;
			CustomTimeWindow window = new CustomTimeWindow(winStart, end, windowTimeout, now);
			synchronized (windowtimouMap) {
				if (windowtimouMap.containsKey(winStart)) {
					windowTimeout = windowtimouMap.get(winStart);
					if (now > windowTimeout) {
						windowtimouMap.put(winStart, windowTimeout + windowWaitTs);
					}
				} else {
					windowTimeout = System.currentTimeMillis() + size;
					windowtimouMap.put(winStart, windowTimeout);
				}
				if (windowtimouMap.size() > windowtimoutLimit) {
					windowtimouMap.clear();
				}
			}
			window.setTimeoutTs(windowTimeout);
			return Collections.singletonList(window);
		} else {
			throw new RuntimeException(
				"Record has Long.MIN_VALUE timestamp (= no timestamp marker). "
					+ "Is the time characteristic set to 'ProcessingTime', or did you forget to call "
					+ "'DataStream.assignTimestampsAndWatermarks(...)'?");
		}
	}

	@Override
	public Trigger<Object, CustomTimeWindow> getDefaultTrigger(
		StreamExecutionEnvironment env) {
		return CustomEventTimeTrigger.create(this);
	}

	@Override
	public TypeSerializer<CustomTimeWindow> getWindowSerializer(
		ExecutionConfig executionConfig) {
		return new CustomTimeWindow.Serializer();
	}

	@Override
	public boolean isEventTime() {
		return true;
	}

	public void clean(long windowStart) {
		synchronized (windowtimouMap) {
			windowtimouMap.remove(windowStart);
		}
	}

	/**
	 * Creates a new {@code CustomTumblingEventTimeWindows} {@link WindowAssigner}
	 * that assigns elements to time windows based on the element timestamp.
	 *
	 * @param size The size of the generated windows.
	 * @return The time policy.
	 */
	public static CustomTumblingEventTimeWindows of(Time size) {
		return new CustomTumblingEventTimeWindows(size.toMilliseconds(), 0);
	}

	/**
	 * Creates a new {@code CustomTumblingEventTimeWindows} {@link WindowAssigner}
	 * that assigns elements to time windows based on the element timestamp and
	 * offset.
	 * For example, if you want window a stream by hour,but window begins at the
	 * 15th minutes of each hour, you can use
	 * {@code of(Time.hours(1),Time.minutes(15))},then you will get time windows
	 * start at 0:15:00,1:15:00,2:15:00,etc.
	 * Rather than that,if you are living in somewhere which is not using
	 * UTC±00:00 time, such as China which is using UTC+08:00,and you want a time
	 * window with size of one day, and window begins at every 00:00:00 of local
	 * time,you may use {@code of(Time.days(1),Time.hours(-8))}. The parameter of
	 * offset is {@code Time.hours(-8))} since UTC+08:00 is 8 hours earlier than
	 * UTC time.
	 *
	 * @param size   The size of the generated windows.
	 * @param offset The offset which window start would be shifted by.
	 * @return The time policy.
	 */
	public static CustomTumblingEventTimeWindows of(Time size, Time offset) {
		return new CustomTumblingEventTimeWindows(size.toMilliseconds(),
			offset.toMilliseconds());
	}
}
