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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

import static org.apache.flink.runtime.leaderelection.ZooKeeperLeaderElectionService.getElectionNodePath;
import static org.apache.flink.util.Preconditions.checkNotNull;

public class ZooKeeperStoreClient {
	private final CuratorFramework client;
	private final String latchPath;
	private final String storePath;

	public ZooKeeperStoreClient(CuratorFramework client, String latchPath, String storePath) throws Exception {
		this.client = checkNotNull(client, "client");
		this.latchPath = checkNotNull(latchPath, "latchPath");
		this.storePath = checkNotNull(storePath, "storePath");

		client.createContainers(latchPath);
		client.createContainers(storePath);
	}

	public void put(@Nonnull UUID sessionId, @Nonnull String path, @Nonnull byte[] data, @Nonnull CreateMode mode) throws Exception {
		String electionNodePath = getElectionNodePath(latchPath, sessionId);
		String storeNodePath = ZKPaths.makePath(storePath, path);

		client.inTransaction()
			.check().forPath(electionNodePath).and()
			.create().withMode(mode).forPath(storeNodePath, data).and()
			.commit();
	}

	public byte[] replace(@Nonnull UUID sessionId, @Nonnull String path, @Nonnull byte[] data, int version) throws Exception {
		byte[] oldData = get(path);

		String electionNodePath = getElectionNodePath(latchPath, sessionId);
		String storeNodePath = ZKPaths.makePath(storePath, path);

		client.inTransaction()
			.check().forPath(electionNodePath).and()
			.setData().withVersion(version).forPath(storeNodePath, data).and()
			.commit();

		return oldData;
	}

	public void remove(@Nonnull UUID sessionId, @Nonnull String path) throws Exception {
		String electionNodePath = getElectionNodePath(latchPath, sessionId);
		String storeNodePath = ZKPaths.makePath(storePath, path);

		client.inTransaction()
			.check().forPath(electionNodePath).and()
			.delete().forPath(storeNodePath).and()
			.commit();
	}

	public List<String> getChildren() throws Exception {
		return client.getChildren().forPath(ZKPaths.makePath(storePath, ""));
	}

	public byte[] get(@Nonnull String path) throws Exception {
		String storeNodePath = ZKPaths.makePath(storePath, path);

		return client.getData().forPath(storeNodePath);
	}

	public int exist(@Nonnull String path) throws Exception {
		String storeNodePath = ZKPaths.makePath(storePath, path);

		Stat stat = client.checkExists().forPath(storeNodePath);

		if (stat != null) {
			return stat.getVersion();
		}

		return -1;
	}
}
