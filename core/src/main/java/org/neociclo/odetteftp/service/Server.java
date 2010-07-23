/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id$
 */
package org.neociclo.odetteftp.service;

import static org.neociclo.odetteftp.util.OftpUtil.toHexString;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.bootstrap.ServerBootstrap;
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
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public abstract class Server {

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

        timer = new HashedWheelTimer();
        activeChildChannels = new DefaultChannelGroup("activeChildChannels @ Server [id: 0x" + toHexString(getId())
                + "]");

        ChannelPipelineFactory pipelineFactory = getPipelineFactory(oftpletFactory,
                timer, activeChildChannels);

        ServerBootstrap bootstrap = new ServerBootstrap(factory);
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

                // TODO maybe safe to include a timeout argument here
                closeActiveChannels.awaitUninterruptibly();

            } else {
    
                ChannelGroupFuture activeChannelsCloseFuture = getChannelGroupCloseFuture(activeChildChannels);

                // TODO maybe safe to include a timeout argument here
                activeChannelsCloseFuture.awaitUninterruptibly();

            }

            timer.stop();
            channel.getFactory().releaseExternalResources();

            channel = null;
            timer = null;
        }

        LOGGER.info("[{}] Odette FTP is stopped.", this);

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

    protected abstract SocketAddress getAddress();

    protected abstract ChannelPipelineFactory getPipelineFactory(OftpletFactory oftpletFactory, Timer timer,
            ChannelGroup channelGroup);

    protected abstract ServerChannelFactory createServerChannelFactory();
}
