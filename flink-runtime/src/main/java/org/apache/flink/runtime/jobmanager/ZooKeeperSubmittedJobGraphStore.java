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

package org.apache.flink.runtime.jobmanager;

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.state.RetrievableStateHandle;
import org.apache.flink.runtime.zookeeper.RetrievableStateStorageHelper;
import org.apache.flink.runtime.zookeeper.ZooKeeperStoreClient;
import org.apache.flink.util.FlinkException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.flink.util.InstantiationUtil.deserializeObject;
import static org.apache.flink.util.InstantiationUtil.serializeObject;
import static org.apache.flink.util.Preconditions.checkState;

/**
 * {@link SubmittedJobGraph} instances for JobManagers running in {@link HighAvailabilityMode#ZOOKEEPER}.
 *
 * <p>Each job graph creates ZNode:
 * <pre>
 * +----O /flink/jobgraphs/&lt;job-id&gt; 1 [persistent]
 * .
 * .
 * .
 * +----O /flink/jobgraphs/&lt;job-id&gt; N [persistent]
 * </pre>
 *
 * <p>The root path is watched to detect concurrent modifications in corner situations where
 * multiple instances operate concurrently. The job manager acts as a {@link SubmittedJobGraphListener}
 * to react to such situations.
 */
public class ZooKeeperSubmittedJobGraphStore implements SubmittedJobGraphStore {

	private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperSubmittedJobGraphStore.class);

	/** Submitted job graphs in ZooKeeper. */
	private final ZooKeeperStoreClient jobGraphStore;

	private final RetrievableStateStorageHelper<SubmittedJobGraph> storage;

	/** The full configured base path including the namespace. */
	private final String zooKeeperFullBasePath;

	/** Flag indicating whether this instance is running. */
	private boolean isRunning;

	/**
	 * Submitted job graph store backed by ZooKeeper.
	 *
	 * @param client ZooKeeper client
	 * @param storePath ZooKeeper path for current job graphs
	 * @param latchPath ZooKeeper path for election nodes
	 * @param storage State storage used to persist the submitted jobs
	 * @throws Exception Throws if constructor fails
	 */
	public ZooKeeperSubmittedJobGraphStore(
		@Nonnull CuratorFramework client,
		@Nonnull String storePath,
		@Nonnull String latchPath,
		@Nonnull RetrievableStateStorageHelper<SubmittedJobGraph> storage
	) throws Exception {
		this.storage = storage;
		this.zooKeeperFullBasePath = client.getNamespace() + storePath;
		this.jobGraphStore = new ZooKeeperStoreClient(client, latchPath, storePath);
	}

	@Override
	public synchronized void start() throws Exception {
		isRunning = true;
	}

	@Override
	public synchronized void stop() throws Exception {
		isRunning = false;
	}

	@Override
	@Nullable
	public synchronized SubmittedJobGraph recoverJobGraph(@Nonnull JobID jobId) throws Exception {
		checkState(isRunning, "JobGraph store is not running.");

		if (LOG.isDebugEnabled()) {
			LOG.debug("Recovering job graph {} from {}/{}.", jobId, zooKeeperFullBasePath, jobId);
		}

		RetrievableStateHandle<SubmittedJobGraph> handle;

		try {
			byte[] data = jobGraphStore.get(jobId.toString());
			handle = deserializeObject(data, Thread.currentThread().getContextClassLoader());
		} catch (KeeperException.NoNodeException ignored) {
			return null;
		} catch (Exception e) {
			throw new FlinkException("Could not retrieve the submitted job graph state handle for "
				+ jobId + " from the submitted job graph store.", e);
		}

		SubmittedJobGraph jobGraph;

		try {
			jobGraph = handle.retrieveState();
		} catch (ClassNotFoundException cnfe) {
			throw new FlinkException("Could not retrieve submitted JobGraph from state handle under " + jobId +
				". This indicates that you are trying to recover from state written by an " +
				"older Flink version which is not compatible. Try cleaning the state handle store.", cnfe);
		} catch (IOException ioe) {
			throw new FlinkException("Could not retrieve submitted JobGraph from state handle under " + jobId +
				". This indicates that the retrieved state handle is broken. Try cleaning the state handle " +
				"store.", ioe);
		}

		LOG.info("Recovered {}.", jobGraph);

		return jobGraph;
	}

	@Override
	public void putJobGraph(@Nonnull UUID sessionId, @Nonnull SubmittedJobGraph jobGraph) throws Exception {
		JobID jobID = jobGraph.getJobId();

		LOG.debug("Adding job graph {} to {}/{}.", jobID, zooKeeperFullBasePath, jobID);

		while (true) {
			synchronized (this) {
				checkState(isRunning, "JobGraph store is not running.");

				int currentVersion = jobGraphStore.exist(jobID.toString());

				if (currentVersion == -1) {
					try {
						RetrievableStateHandle<SubmittedJobGraph> handle = storage.store(jobGraph);
						byte[] data = serializeObject(handle);

						jobGraphStore.put(sessionId, jobID.toString(), data, CreateMode.PERSISTENT);

						break;
					} catch (KeeperException.NodeExistsException ignored) {
						// continue
					}
				} else {
					RetrievableStateHandle<SubmittedJobGraph> handle = null;
					RetrievableStateHandle<SubmittedJobGraph> oldHandle = null;

					boolean success = false;
					try {
						handle = storage.store(jobGraph);
						byte[] data = serializeObject(handle);

						byte[] oldData = jobGraphStore.replace(sessionId, jobID.toString(), data, currentVersion);
						oldHandle = deserializeObject(oldData, Thread.currentThread().getContextClassLoader());

						success = true;
						break;
					} catch (KeeperException.NoNodeException ignored) {
						// continue
					} finally {
						if (success && (oldHandle != null)) {
							oldHandle.discardState();
						}

						if (!success && (handle != null)) {
							handle.discardState();
						}
					}
				}
			}
		}

		LOG.info("Added {} to ZooKeeper.", jobGraph);
	}

	@Override
	public synchronized void removeJobGraph(UUID sessionId, @Nonnull JobID jobId) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Removing job graph {} from {}/{}.", jobId, zooKeeperFullBasePath, jobId);
		}

		RetrievableStateHandle<SubmittedJobGraph> handle = null;

		try {
			byte[] data = jobGraphStore.get(jobId.toString());
			handle = deserializeObject(data, Thread.currentThread().getContextClassLoader());

			jobGraphStore.remove(sessionId, jobId.toString());
		} finally {
			if (handle != null) {
				handle.discardState();
			}
		}

		LOG.info("Removed job graph {} from ZooKeeper.", jobId);
	}

	@Override
	public Collection<JobID> getJobIds() throws Exception {
		Collection<String> paths;

		LOG.debug("Retrieving all stored job ids from ZooKeeper under {}.", zooKeeperFullBasePath);

		try {
			paths = jobGraphStore.getChildren();
		} catch (Exception e) {
			throw new Exception("Failed to retrieve entry paths from ZooKeeperStateHandleStore.", e);
		}

		return paths.stream()
			.map(JobID::fromHexString)
			.collect(Collectors.toList());
	}
}
