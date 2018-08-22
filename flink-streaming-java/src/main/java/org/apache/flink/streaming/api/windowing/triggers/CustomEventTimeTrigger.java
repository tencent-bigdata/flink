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

package org.apache.flink.streaming.api.windowing.triggers;

import org.apache.flink.streaming.api.windowing.assigners.CustomTumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.CustomTimeWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Custom event time trigger.
 */
public class CustomEventTimeTrigger extends Trigger<Object, CustomTimeWindow> {

	private static final Logger LOG = LoggerFactory.getLogger(CustomEventTimeTrigger.class);

	private static final long serialVersionUID = 1L;

	private CustomTumblingEventTimeWindows windowAssigner;

	public CustomEventTimeTrigger(CustomTumblingEventTimeWindows windowAssigner) {
		super();
		this.windowAssigner = windowAssigner;
		LOG.info("CustomEventTimeTrigger has been used ");
	}

	@Override
	public TriggerResult onElement(Object element, long timestamp,
		CustomTimeWindow window,
		org.apache.flink.streaming.api.windowing.triggers.Trigger.TriggerContext ctx)
		throws Exception {
		long now = System.currentTimeMillis();
		if (now >= window.getTimeoutTs()
			&& window.maxTimestamp() <= ctx.getCurrentWatermark()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(CustomEventTimeTrigger.class.getCanonicalName()
					+ "  onElement fire_and_purge message:" + element);
			}
			windowAssigner.clean(window.getStart());
			return TriggerResult.FIRE_AND_PURGE;
		}
		if (LOG.isDebugEnabled()) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			LOG.debug(CustomEventTimeTrigger.class.getCanonicalName()
				+ " onElement continue message:" + element + ",now:"
				+ sdf.format(new Date(now)) + ",winTimeout:"
				+ sdf.format(new Date(window.getTimeoutTs())) + "timestamp:"
				+ sdf.format(new Date(ctx.getCurrentWatermark())));
		}
		ctx.registerEventTimeTimer(window.maxTimestamp());
		return TriggerResult.CONTINUE;
	}

	@Override
	public TriggerResult onProcessingTime(long time, CustomTimeWindow window,
		org.apache.flink.streaming.api.windowing.triggers.Trigger.TriggerContext ctx) throws Exception {
		return TriggerResult.CONTINUE;
	}

	@Override
	public TriggerResult onEventTime(long time, CustomTimeWindow window,
		org.apache.flink.streaming.api.windowing.triggers.Trigger.TriggerContext ctx)
		throws Exception {
		if (time >= window.maxTimestamp()
			|| System.currentTimeMillis() > window.getTimeoutTs()) {
			windowAssigner.clean(window.getStart());
			if (LOG.isDebugEnabled()) {
				LOG.debug(CustomEventTimeTrigger.class.getCanonicalName()
					+ " onEventTime fire_and_purge ");
			}
			return TriggerResult.FIRE_AND_PURGE;
		}
		if (LOG.isDebugEnabled()) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			LOG.debug(CustomEventTimeTrigger.class.getCanonicalName()
				+ " onEventTime continue " + ",time:" + sdf.format(new Date(time))
				+ ",winMaxTimestamp:" + sdf.format(new Date(window.maxTimestamp())));
		}
		return TriggerResult.CONTINUE;
	}

	@Override
	public void clear(CustomTimeWindow window,
		org.apache.flink.streaming.api.windowing.triggers.Trigger.TriggerContext ctx)
		throws Exception {
		ctx.deleteEventTimeTimer(window.maxTimestamp());
	}

	/**
	 * Creates an custom event-time trigger that fires once the watermark passes
	 * the end of the window. Once the trigger fires all elements are discarded.
	 * Elements that arrive late immediately trigger window evaluation with just
	 * this one element.
	 */
	public static CustomEventTimeTrigger create(
		CustomTumblingEventTimeWindows windowAssigner) {
		return new CustomEventTimeTrigger(windowAssigner);
	}

}
