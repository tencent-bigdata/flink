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

package org.apache.flink.runtime.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.apache.flink.runtime.leaderelection.ZooKeeperLeaderElectionService.getElectionNodePath;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ZooKeeperStoreClientTest {

	private static final ZooKeeperTestEnvironment ZOOKEEPER = new ZooKeeperTestEnvironment(1);

	@AfterClass
	public static void tearDown() throws Exception {
		ZOOKEEPER.shutdown();
	}

	@Before
	public void cleanUp() throws Exception {
		ZOOKEEPER.deleteAll();
	}

	@Test
	public void testPrimitivePut() throws Exception {
		String latchPath = "/latch";
		String storePath = "/store";
		UUID sessionId = new UUID(0, 0);
		UUID invalidSessionId = new UUID(0, 1);

		String electionNodePath = getElectionNodePath(latchPath, sessionId);
		ZOOKEEPER.getClient().create().creatingParentContainersIfNeeded().forPath(electionNodePath);

		ZooKeeperStoreClient client = new ZooKeeperStoreClient(ZOOKEEPER.getClient(), latchPath, storePath);
		byte[] exceptedData = new byte[]{ 1, 2, 3, -1, 4 };
		String path = "data_0";
		client.put(sessionId, path, exceptedData, CreateMode.PERSISTENT);

		assertArrayEquals(exceptedData, client.get(path));

		try {
			client.put(sessionId, path, exceptedData, CreateMode.PERSISTENT);
			fail("Should cause KeeperException.NodeExistsException.");
		} catch (KeeperException.NodeExistsException e) {
			// pass
		}

		try {
			client.put(invalidSessionId, path, exceptedData, CreateMode.PERSISTENT);
			fail("Should cause KeeperException.NoNodeException.");
		} catch (KeeperException.NoNodeException e) {
			// pass
		}
	}

	@Test
	public void testPrimitiveReplace() throws Exception {
		String latchPath = "/latch";
		String storePath = "/store";
		UUID sessionId = new UUID(0, 0);
		UUID invalidSessionId = new UUID(0, 1);

		String electionNodePath = getElectionNodePath(latchPath, sessionId);
		ZOOKEEPER.getClient().create().creatingParentContainersIfNeeded().forPath(electionNodePath);

		ZooKeeperStoreClient client = new ZooKeeperStoreClient(ZOOKEEPER.getClient(), latchPath, storePath);
		byte[] exceptedData = new byte[]{ 1, 2, 3, -1, 4 };
		String path = "data_0";
		client.put(sessionId, path, exceptedData, CreateMode.PERSISTENT);

		assertArrayEquals(exceptedData, client.get(path));

		int version = client.exist(path);
		assertNotEquals(-1, version);

		byte[] replacedData = new byte[]{ -1, 42, 103, 44 };
		client.replace(sessionId, path, replacedData, version);

		assertArrayEquals(replacedData, client.get(path));

		try {
			// now `version` is an invalid version
			client.replace(sessionId, path, replacedData, version);
			fail("Should cause KeeperException.BadVersionException.");
		} catch (KeeperException.BadVersionException e) {
			// pass
		}

		try {
			int newVersion = client.exist(path);
			assertNotEquals(-1, version);
			client.replace(invalidSessionId, path, exceptedData, newVersion);
			fail("Should cause KeeperException.NoNodeException.");
		} catch (KeeperException.NoNodeException e) {
			// pass
		}
	}

	@Test
	public void testPrimitiveRemove() throws Exception {
		String latchPath = "/latch";
		String storePath = "/store";
		UUID sessionId = new UUID(0, 0);
		UUID invalidSessionId = new UUID(0, 1);

		String electionNodePath = getElectionNodePath(latchPath, sessionId);
		ZOOKEEPER.getClient().create().creatingParentContainersIfNeeded().forPath(electionNodePath);

		ZooKeeperStoreClient client = new ZooKeeperStoreClient(ZOOKEEPER.getClient(), latchPath, storePath);
		byte[] exceptedData = new byte[]{ 1, 2, 3, -1, 4 };
		String path = "data_0";
		client.put(sessionId, path, exceptedData, CreateMode.PERSISTENT);

		assertArrayEquals(exceptedData, client.get(path));

		try {
			client.remove(invalidSessionId, path);
			fail("Should cause KeeperException.NoNodeException.");
		} catch (KeeperException.NoNodeException e) {
			// pass
		}

		assertNotEquals(-1, client.exist(path));
		client.remove(sessionId, path);
		assertEquals(-1, client.exist(path));

		try {
			client.remove(sessionId, path);
			fail("Should cause KeeperException.NoNodeException.");
		} catch (KeeperException.NoNodeException e) {
			// pass
		}
	}

	@Test
	public void testPrimitiveGetChildren() throws Exception {
		int childrenNum = 10;

		String latchPath = "/latch";
		String storePath = "/store";
		UUID sessionId = new UUID(0, 0);

		String electionNodePath = getElectionNodePath(latchPath, sessionId);
		ZOOKEEPER.getClient().create().creatingParentContainersIfNeeded().forPath(electionNodePath);

		ZooKeeperStoreClient client = new ZooKeeperStoreClient(ZOOKEEPER.getClient(), latchPath, storePath);

		List<String> children = client.getChildren();
		assertTrue(children.isEmpty());

		byte[] exceptedData = new byte[]{ 1, 2, 3, -1, 4 };
		String pathPrefix = "data_";

		for (int i = 0; i < childrenNum; i++) {
			client.put(sessionId, pathPrefix + i, exceptedData, CreateMode.PERSISTENT);
		}

		children = client.getChildren();
		assertEquals(childrenNum, children.size());

		for (String path : children) {
			assertArrayEquals(exceptedData, client.get(path));
			client.remove(sessionId, path);
		}

		children = client.getChildren();
		assertTrue(children.isEmpty());
	}

	@Test
	public void testPrimitiveGet() throws Exception {
		String latchPath = "/latch";
		String storePath = "/store";
		UUID sessionId = new UUID(0, 0);

		String electionNodePath = getElectionNodePath(latchPath, sessionId);
		ZOOKEEPER.getClient().create().creatingParentContainersIfNeeded().forPath(electionNodePath);

		ZooKeeperStoreClient client = new ZooKeeperStoreClient(ZOOKEEPER.getClient(), latchPath, storePath);
		byte[] exceptedData = new byte[]{ 1, 2, 3, -1, 4 };
		String path = "data_0";
		client.put(sessionId, path, exceptedData, CreateMode.PERSISTENT);

		byte[] actualData = client.get(path);
		assertArrayEquals(exceptedData, actualData);

		String invalidPath = "invalidPath";
		try {
			client.get(invalidPath);
			fail("Should cause KeeperException.NoNodeException.");
		} catch (KeeperException.NoNodeException e) {
			assertTrue(e.getMessage().contains(invalidPath));
		}
	}

	@Test
	public void testPrimitiveExist() throws Exception {
		String latchPath = "/latch";
		String storePath = "/store";
		UUID sessionId = new UUID(0, 0);

		String electionNodePath = getElectionNodePath(latchPath, sessionId);
		ZOOKEEPER.getClient().create().creatingParentContainersIfNeeded().forPath(electionNodePath);

		ZooKeeperStoreClient client = new ZooKeeperStoreClient(ZOOKEEPER.getClient(), latchPath, storePath);
		byte[] exceptedData = new byte[]{ 1, 2, 3, -1, 4 };
		String path = "data_0";
		client.put(sessionId, path, exceptedData, CreateMode.PERSISTENT);

		byte[] actualData = client.get(path);
		assertArrayEquals(exceptedData, actualData);

		int version = client.exist(path);
		assertNotEquals(-1, version);

		String invalidPath = "invalidPath";
		int invalidVersion = client.exist(invalidPath);
		assertEquals(-1, invalidVersion);
	}
}
