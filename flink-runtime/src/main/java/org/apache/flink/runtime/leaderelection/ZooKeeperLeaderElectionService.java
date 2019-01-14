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

package org.apache.flink.runtime.leaderelection;

import org.apache.flink.annotation.VisibleForTesting;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.framework.recipes.AfterConnectionEstablished;
import org.apache.curator.framework.recipes.locks.LockInternals;
import org.apache.curator.framework.recipes.locks.LockInternalsSorter;
import org.apache.curator.framework.recipes.locks.StandardLockInternalsDriver;
import org.apache.curator.utils.ThreadUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.flink.util.Preconditions.checkNotNull;
import static org.apache.flink.util.Preconditions.checkState;

/**
 * Leader election service for multiple contender. The leader is elected using ZooKeeper.
 * The current leader's address as well as its leader session ID is published via ZooKeeper as well.
 */
public class ZooKeeperLeaderElectionService implements LeaderElectionService, UnhandledErrorListener {

	private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperLeaderElectionService.class);

	public enum State {
		LATENT,
		STARTED,
		CLOSED
	}

	private final AtomicReference<State> state = new AtomicReference<>(State.LATENT);

	/** Client to the ZooKeeper quorum. */
	private final CuratorFramework client;

	private final String leaderPath;
	private final String latchPath;
	private final String contenderPrefix = "contender-";
	private final AtomicReference<String> ourPath = new AtomicReference<>();
	private final AtomicReference<Future<?>> startTask = new AtomicReference<>();

	private final LockInternalsSorter sorter = StandardLockInternalsDriver::standardFixForSorting;

	private volatile UUID leaderSessionID;

	private final AtomicBoolean hasLeadership = new AtomicBoolean(false);

	/** The leader contender which applies for leadership. */
	private volatile LeaderContender leaderContender;

	private final ConnectionStateListener listener = (client, newState) -> handleStateChange(newState);

	/**
	 * Creates a ZooKeeperLeaderElectionService object.
	 *
	 * @param client Client which is connected to the ZooKeeper quorum
	 * @param latchPath ZooKeeper node path for the leader election latch
	 * @param leaderPath ZooKeeper node path for the node which stores the current leader information
	 */
	public ZooKeeperLeaderElectionService(CuratorFramework client, String latchPath, String leaderPath) {
		this.client = checkNotNull(client, "CuratorFramework client");
		this.leaderPath = checkNotNull(leaderPath, "leaderPath");
		this.latchPath = checkNotNull(latchPath, "latchPath");
		this.leaderSessionID = null;
		this.leaderContender = null;
	}

	@Override
	public void start(LeaderContender contender) throws Exception {
		checkState(state.compareAndSet(State.LATENT, State.STARTED), "Cannot be started more than once");
		checkState(leaderContender == null, "Contender was already set");
		checkNotNull(contender, "Contender must not be null");

		LOG.info("Starting ZooKeeperLeaderElectionService {}.", this);

		Future<?> localStartTask = startTask.getAndSet(AfterConnectionEstablished.execute(client, () -> {
			try {
				internalStart(contender);
			} finally {
				startTask.set(null);
			}
		}));

		if (localStartTask != null) {
			localStartTask.cancel(true);
		}
	}

	private synchronized void internalStart(LeaderContender contender) {
		State localState = state.get();

		if (localState == State.STARTED) {
			client.getConnectionStateListenable().addListener(listener);
			client.getUnhandledErrorListenable().addListener(this);

			leaderContender = contender;

			try {
				reset();
			} catch (Exception e) {
				ThreadUtils.checkInterrupted(e);
				LOG.error("An error occurred checking resetting leadership.", e);
			}
		} else {
			LOG.warn("Call internal start when leader election service {}.", localState);
		}
	}

	@Override
	public synchronized void stop() throws Exception {
		checkState(state.compareAndSet(State.STARTED, State.CLOSED), "Already closed or has not been started");

		LOG.info("Stopping ZooKeeperLeaderElectionService {}.", this);

		Future<?> localStartTask = startTask.getAndSet(null);
		if (localStartTask != null) {
			localStartTask.cancel(true);
		}

		setNode(null);
		notLeader();

		leaderContender = null;

		client.getUnhandledErrorListenable().removeListener(this);
		client.getConnectionStateListenable().removeListener(listener);
	}

	@Override
	public boolean hasLeadership(@Nonnull UUID leaderSessionId) {
		return (state.get() == State.STARTED) && leaderSessionId.equals(this.leaderSessionID) && hasLeadership.get();
	}

	@VisibleForTesting
	void reset() throws Exception {
		notLeader();
		setNode(null);

		BackgroundCallback callback = (client, event) -> {
			if (event.getResultCode() == KeeperException.Code.OK.intValue()) {
				setNode(event.getName());
				if (state.get() == State.CLOSED) {
					setNode(null);
				} else {
					getChildren();
				}
			} else {
				LOG.error("getChildren() failed, resultCode={}.", event.getResultCode());
			}
		};

		client.create().creatingParentContainersIfNeeded()
			.withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
			.inBackground(callback)
			.forPath(ZKPaths.makePath(latchPath, contenderPrefix));
	}

	private void getChildren() throws Exception {
		BackgroundCallback callback = (client, event) -> {
			if (event.getResultCode() == KeeperException.Code.OK.intValue()) {
				checkLeadership(event.getChildren());
			}
		};

		client.getChildren().inBackground(callback).forPath(latchPath);
	}

	private void checkLeadership(List<String> children) throws Exception {
		final String localOurPath = ourPath.get();
		final List<String> sortedChildren = LockInternals.getSortedChildren(contenderPrefix, sorter, children);
		final int ourIndex = (localOurPath != null) ? sortedChildren.indexOf(ZKPaths.getNodeFromPath(localOurPath)) : -1;

		if (ourIndex < 0) {
			LOG.error("Can't find our node. Resetting. Index: {}.", ourIndex);
			reset();
		} else if (ourIndex == 0) {
			isLeader(localOurPath);
		} else {
			String watchPath = sortedChildren.get(ourIndex - 1);
			Watcher watcher = (event) -> {
				if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
					try {
						getChildren();
					} catch (Exception ex) {
						ThreadUtils.checkInterrupted(ex);
						LOG.error("An error occurred checking the leadership.", ex);
					}
				}
			};

			BackgroundCallback callback = (client, event) -> {
				if (event.getResultCode() == KeeperException.Code.NONODE.intValue()) {
					// previous node is gone - reset
					reset();
				}
			};

			// use getData() instead of exists() to avoid leaving unneeded watchers which is a type of resource leak
			client.getData()
				.usingWatcher(watcher)
				.inBackground(callback)
				.forPath(ZKPaths.makePath(latchPath, watchPath));
		}
	}

	private synchronized void isLeader(String electionNodePath) {
		boolean oldValue = hasLeadership.getAndSet(true);

		if (!oldValue) {
			String electionNodeNumber = electionNodePath.substring(
				electionNodePath.indexOf(contenderPrefix) + contenderPrefix.length());

			leaderSessionID = new UUID(0, Long.valueOf(electionNodeNumber));

			if (LOG.isDebugEnabled()) {
				LOG.debug(
					"Grant leadership to contender {} with session ID {}.",
					leaderContender.getAddress(),
					leaderSessionID);
			}

			leaderContender.grantLeadership(leaderSessionID);
		}

		notifyAll();
	}

	private synchronized void notLeader() {
		boolean oldValue = hasLeadership.getAndSet(false);

		if (oldValue) {
			leaderSessionID = null;

			if (LOG.isDebugEnabled()) {
				LOG.debug("Revoke leadership of {}.", leaderContender.getAddress());
			}

			leaderContender.revokeLeadership();
		}

		notifyAll();
	}

	private void setNode(String newValue) throws Exception {
		String oldPath = ourPath.getAndSet(newValue);
		if (oldPath != null) {
			client.delete().guaranteed().inBackground().forPath(oldPath);
		}
	}

	@Override
	public void confirmLeaderSessionID(UUID leaderSessionID) {
		checkNotNull(leaderSessionID);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Confirm leader session ID {} for leader {}.", leaderSessionID, leaderContender.getAddress());
		}

		synchronized (this) {
			if (state.get() == State.STARTED) {
				if (hasLeadership(leaderSessionID)) {
					publishLeaderInfo(leaderSessionID, leaderContender.getAddress());
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Ignoring the leader session Id {} confirmation, since the " +
							"contender is not the leader any more.", leaderSessionID);
					}
				}
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Ignoring the leader session Id {} confirmation, since the " +
						"ZooKeeperLeaderElectionService has already been stopped.", leaderSessionID);
				}
			}
		}
	}

	/**
	 * Writes the current leader's address as well the given leader session ID to ZooKeeper.
	 *
	 * @param leaderSessionID Leader session ID which is written to ZooKeeper
	 * @param leaderAddress Leader address which is written to ZooKeeper
	 */
	private void publishLeaderInfo(UUID leaderSessionID, String leaderAddress) {
		// this method does not have to be synchronized because the curator framework client
		// is thread-safe

		if (LOG.isDebugEnabled()) {
			LOG.debug("Write leader information: Leader={}, session ID={}.", leaderAddress, leaderSessionID);
		}

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);

			oos.writeUTF(leaderAddress);
			oos.writeObject(leaderSessionID);

			oos.close();

			String electionNodePath = electionNodePathFromSessionId(leaderSessionID);

			if (electionNodePath != null) {
				while (hasLeadership(leaderSessionID)) {
					Stat stat = client.checkExists().creatingParentContainersIfNeeded().forPath(leaderPath);
					if (stat != null) {
						long owner = stat.getEphemeralOwner();
						long sessionID = client.getZookeeperClient().getZooKeeper().getSessionId();

						if (owner == sessionID) {
							client.inTransaction()
								.check().forPath(electionNodePath).and()
								.setData().forPath(leaderPath, baos.toByteArray()).and()
								.commit();
							break;
						} else {
							client.inTransaction()
								.check().forPath(electionNodePath).and()
								.delete().forPath(leaderPath).and()
								.commit();
						}
					} else {
						client.inTransaction()
							.check().forPath(electionNodePath).and()
							.create().withMode(CreateMode.EPHEMERAL).forPath(leaderPath, baos.toByteArray()).and()
							.commit();
						break;
					}
				}

				if (LOG.isDebugEnabled()) {
					LOG.debug("Successfully wrote leader information: Leader={}, session ID={}.", leaderAddress, leaderSessionID);
				}
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Abort publishing leader info. Current contender is no longer the leader.");
				}
			}
		} catch (Exception e) {
			leaderContender.handleError(new Exception("Could not write leader address and leader session ID to ZooKeeper.", e));
		}
	}

	private String electionNodePathFromSessionId(UUID leaderSessionID) {
		return String.format("%s/%s%010d", latchPath, contenderPrefix, leaderSessionID.getLeastSignificantBits());
	}

	private void handleStateChange(ConnectionState newState) {
		switch (newState) {
			case CONNECTED:
				LOG.debug("Connected to ZooKeeper quorum. Leader election can start.");
				break;
			case SUSPENDED:
				LOG.warn(
					"Connection to ZooKeeper suspended. The contender {} no longer participates in the leader election.",
					leaderContender.getAddress());

				notLeader();

				break;
			case RECONNECTED:
				LOG.info("Connection to ZooKeeper was reconnected. Leader election can be restarted.");

				try {
					reset();
				} catch (Exception e) {
					ThreadUtils.checkInterrupted(e);

					LOG.error("Could not reset leader latch.", e);

					notLeader();
				}

				break;
			case LOST:
				// Maybe we have to throw an exception here to terminate the JobManager
				LOG.warn(
					"Connection to ZooKeeper lost. The contender {} no longer participates in the leader election.",
					leaderContender.getAddress());

				notLeader();

				break;
		}
	}

	@Override
	public void unhandledError(String message, Throwable e) {
		leaderContender.handleError(new Exception("Unhandled error in ZooKeeperLeaderElectionService: " + message, e));
	}

	@Override
	public String toString() {
		return "ZooKeeperLeaderElectionService{" +
			"leaderPath='" + leaderPath + '\'' +
			'}';
	}

	// -------------------- Testing Methods --------------------

	@VisibleForTesting
	UUID getLeaderSessionID() {
		return leaderSessionID;
	}
}
