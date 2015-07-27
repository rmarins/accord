/**
 * The Accord Project, http://accordproject.org
 * Copyright (C) 2005-2013 Rafael Marins, http://rafaelmarins.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neociclo.odetteftp.service;

import static org.neociclo.odetteftp.util.OftpUtil.toHexString;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ServerChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroupFuture;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 */
public abstract class Server extends BaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private static final ConcurrentMap<Integer, Server> allServers = new ConcurrentHashMap<Integer, Server>();

    private static Integer allocateId(Server server) {
        Integer id = Integer.valueOf(System.identityHashCode(server));
        for (;;) {
            // Loop until a unique ID is acquired.
            // It should be found in one loop practically.
            if (allServers.putIfAbsent(id, server) == null) {
                // Successfully acquired.
                return id;
            } else {
                // Taken by other server at almost the same moment.
                id = Integer.valueOf(id.intValue() + 1);
            }
        }
    }

    private static ChannelGroupFuture getChannelGroupCloseFuture(ChannelGroup g) {
        Collection<ChannelFuture> futures = new ArrayList<ChannelFuture>(g.size());

        for (Channel c: g) {
            futures.add(c.getCloseFuture());
        }

        return new DefaultChannelGroupFuture(g, futures);
    }

    private OftpletFactory oftpletFactory;

    private ChannelGroup activeChildChannels;
    private Timer timer;

    private Channel channel;
    private Integer id;

    /**
     * Embedded class constructor taking service configuration and callback
     * handler.
     */
    public Server(OftpletFactory oftpletFactory) {
        super();

        if (oftpletFactory == null) {
            throw new NullPointerException("oftpletFactory");
        }

        this.oftpletFactory = oftpletFactory;
        this.id = allocateId(this);
    }

    public synchronized void start() throws Exception {

        if (channel != null && channel.isBound()) {
            throw new IllegalStateException("Odette FTP is already active.");
        }

        LOGGER.info("[{}] Starting ODETTE-FTP service ...", this);

        ServerChannelFactory factory = createServerChannelFactory();

        if (timer == null) {
        	timer = new HashedWheelTimer();
        	setManaged(timer);
        }

        activeChildChannels = new DefaultChannelGroup("activeChildChannels @ Server [id: 0x" + toHexString(getId())
                + "]");

        ChannelPipelineFactory pipelineFactory = getPipelineFactory(oftpletFactory,
                timer, activeChildChannels);

        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        if (isExplicitUseHeapBufferFactory()) {
        	bootstrap.setOption("child.bufferFactory", HeapChannelBufferFactory.getInstance());
        }
        bootstrap.setPipelineFactory(pipelineFactory);

        channel = bootstrap.bind(getAddress());

        LOGGER.info("[{}] Odette FTP service started. Bound on: {}", this, channel.getLocalAddress());

    }

    public synchronized void stop() {
        stop(false);
    }

    public synchronized void stop(boolean emergencyCloseDown) {

        LOGGER.info("[{}] Stopping Odette FTP service...", this);

        if (timer != null) {
        }

        if (channel != null) {

            // stop accepting incoming connections
            channel.unbind();

            if (emergencyCloseDown) {

                ChannelGroupFuture closeActiveChannels = activeChildChannels.close();

                // TODO send Emergency Close Down End Session to all channels open

                // MPA : add 1 minute timeout 
                closeActiveChannels.awaitUninterruptibly(60*1000);

            } else {
    
                ChannelGroupFuture activeChannelsCloseFuture = getChannelGroupCloseFuture(activeChildChannels);

                // MPA : add 1 minute timeout 
                activeChannelsCloseFuture.awaitUninterruptibly(60*1000);

            }

            releaseExternalResources();

            channel = null;
            timer = null;
        }

        LOGGER.info("[{}] Odette FTP is stopped.", this);

    }

	protected void releaseExternalResources() {
        if (isManaged(timer)) {
        	timer.stop();
        }
        channel.getFactory().releaseExternalResources();
	}

	public boolean isStarted() {
        return !isStopped();
    }

    public boolean isStopped() {
        return (channel == null || channel.isBound());
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName());
        sb.append("(id: 0x").append(toHexString(getId())).append(")");
        return sb.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        allServers.remove(getId());
    }

    public Timer getTimer() {
        return timer;
    }

	/**
	 * The Timer which was specified should be stopped manually by calling
	 * {@link Timer#stop()} when your application shuts down.
	 * 
	 * @param timer
	 */
    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    protected abstract SocketAddress getAddress();

    protected abstract ChannelPipelineFactory getPipelineFactory(OftpletFactory oftpletFactory, Timer timer,
            ChannelGroup channelGroup);

    protected abstract ServerChannelFactory createServerChannelFactory();
}
