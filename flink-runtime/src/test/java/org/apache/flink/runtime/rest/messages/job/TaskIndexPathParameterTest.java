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

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link TaskIndexPathParameter}.
 */
public class TaskIndexPathParameterTest {

	private TaskIndexPathParameter subtaskIndexPathParameter;

	@Before
	public void setUp() {
		subtaskIndexPathParameter = new TaskIndexPathParameter();
	}

	@Test
	public void testConversionFromString() throws Exception {
		assertThat(subtaskIndexPathParameter.convertFromString("2147483647"), equalTo(Integer.MAX_VALUE));
	}

	@Test
	public void testConversionFromStringNegativeNumber() throws Exception {
		try {
			subtaskIndexPathParameter.convertFromString("-2147483648");
			fail("Expected exception not thrown");
		} catch (final ConversionException e) {
			assertThat(e.getMessage(), equalTo("Task index must be positive, was: " + Integer
				.MIN_VALUE));
		}
	}

	@Test
	public void testConvertToString() throws Exception {
		assertThat(subtaskIndexPathParameter.convertToString(Integer.MAX_VALUE), equalTo("2147483647"));
	}

	@Test
	public void testIsMandatoryParameter() {
		assertTrue(subtaskIndexPathParameter.isMandatory());
	}

}
