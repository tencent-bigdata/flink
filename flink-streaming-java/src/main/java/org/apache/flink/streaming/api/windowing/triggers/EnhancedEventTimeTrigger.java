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

import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced event-time trigger.
 */
public class EnhancedEventTimeTrigger extends Trigger<Object, TimeWindow> {

	private static final Logger LOG = LoggerFactory.getLogger(EnhancedEventTimeTrigger.class);

	private static final long serialVersionUID = 1L;

	private EnhancedEventTimeTrigger() {}

	@Override
	public TriggerResult onElement(Object element, long timestamp,
			TimeWindow window,
			TriggerContext ctx) throws Exception {
		long currentWatermark = ctx.getCurrentWatermark();
		if (window.maxTimestamp() <= currentWatermark) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Receiving late element: {}, current watermark: {}",
					element, currentWatermark);
			}
			if (ctx.numEventTimeTimers() == 0) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Registering timer: {}",
						currentWatermark + window.getEnd() - window.getStart());
				}
				ctx.registerEventTimeTimer(currentWatermark
					+ window.getEnd() - window.getStart());
			}
		} else {
			ctx.registerEventTimeTimer(window.maxTimestamp());
		}
		return TriggerResult.CONTINUE;
	}

	@Override
	public TriggerResult onProcessingTime(long time, TimeWindow window,
			TriggerContext ctx) throws Exception {
		return TriggerResult.CONTINUE;
	}

	@Override
	public TriggerResult onEventTime(long time, TimeWindow window,
			TriggerContext ctx) throws Exception {
		if (time != window.maxTimestamp()) {
			LOG.warn("Triggering late {} on {}", window, time);
		}
		ctx.deleteEventTimeTimer(time);
		return TriggerResult.FIRE_AND_PURGE;
	}

	@Override
	public void clear(TimeWindow window, TriggerContext ctx) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Invoking EnhancedEventTimeTrigger#clear for {}", window);
		}
		ctx.deleteEventTimeTimer(window.maxTimestamp());
	}

	public static EnhancedEventTimeTrigger create() {
		return new EnhancedEventTimeTrigger();
	}

}
