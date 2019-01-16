package org.apache.flink.runtime.executiongraph;

import org.apache.flink.core.testutils.CommonTestUtils;
import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.taskmanager.TaskManagerLocation;
import org.junit.Test;

import java.net.InetAddress;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link ExceptionTrace}.
 */
public class ExceptionTraceTest {

	@Test
	public void testJavaSerializable() throws Exception {
		Random random = new Random();
		ExceptionTrace exceptionTrace = createRandomFailureTrace(random);

		ExceptionTrace exceptionTraceCopy = CommonTestUtils.createCopySerializable(exceptionTrace);
		assertEquals(exceptionTrace, exceptionTraceCopy);
		assertEquals(exceptionTrace.hashCode(), exceptionTraceCopy.hashCode());
	}

	static ExceptionTrace createRandomFailureTrace(Random random) {
		return new ExceptionTrace(
			random.nextLong(),
			new Exception("exception" + random.nextInt()),
			new ExecutionAttemptID(),
			"task" + random.nextInt(),
			random.nextInt(),
			new TaskManagerLocation(new ResourceID("resource" + random.nextInt()), InetAddress.getLoopbackAddress(), random.nextInt(1024) + 1)
		);
	}
}
