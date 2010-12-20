/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2010 Neociclo, http://www.neociclo.com
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

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public abstract class Client extends BaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private OftpletFactory oftpletFactory;
    private long idleTimeout = TimeUnit.SECONDS.toMillis(90);

    private Channel channel;
    private Timer timer;

    private boolean connected;

    protected Runnable disconnectListener;

    public Client(OftpletFactory oftpletFactory) {
    	if (oftpletFactory == null) {
    		throw new IllegalArgumentException("OftpletFactory cannot be null");
    	}
        this.oftpletFactory = oftpletFactory;
    }

    public synchronized void connect() throws Exception {
        connect(false);
    }

    public synchronized void connect(boolean await) throws Exception {

        ChannelFactory factory = createChannelFactory();

        if (timer == null) {
            timer = new HashedWheelTimer();
            setManaged(timer);
            LOGGER.trace("Managed timer acquired: {}", timer);
        }

        ChannelPipelineFactory pipelineFactory = getPipelineFactory(oftpletFactory, timer);

        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        bootstrap.setPipelineFactory(pipelineFactory);

        LOGGER.info("Connecting to ODETTE-FTP service on {}...", getRemoteAddress());

        ChannelFuture connectFuture = bootstrap.connect(getRemoteAddress(), getLocalAddress());

        ChannelFutureListener setConnectedOnOpen = new ChannelFutureListener() {
            public void operationComplete(ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                    LOGGER.info("Connected.");
                    setConnected();
                }
            }
        };
        connectFuture.addListener(setConnectedOnOpen);

        // await to get connected for an idle timeout period
        try {
            connectFuture.await(idleTimeout);
        } catch (InterruptedException e) {
            LOGGER.info("Connecting timeout.");
            throw e;
        }

        Channel c = connectFuture.getChannel();
        if (!c.isConnected()) {
        	releaseExternalResources();
            LOGGER.info("Connection failed. Channel is not connected: {} ", c);
            throw new Exception("Channel is not connected.");
        }

        this.channel = c;

        // setup disconnect on close listener 
        ChannelFuture closeFuture = c.getCloseFuture();
        ChannelFutureListener setDisconnectedOnClose = new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                LOGGER.info("Disconnected.");
                setDisconnected();
                if (getDisconnectListener() != null) {
                	getDisconnectListener().run();
                }
            }
        };
        closeFuture.addListener(setDisconnectedOnClose);

        // need await disconnect
        if (await) {
            awaitDisconnect();
            releaseExternalResources();
        }

    }

    public synchronized void awaitDisconnect() {
        if (channel == null) {
            throw new IllegalStateException("The connect() method were not invoked.");
        }
        ChannelFuture closeFuture = channel.getCloseFuture();
        closeFuture.awaitUninterruptibly();
    }

    public boolean isConnected() {
        return connected;
    }

    private void setConnected() {
        connected = true;
    }

    private void setDisconnected() {
        connected = false;
        channel = null;
    }

    public void disconnect() throws Exception {

        ChannelFuture closeFuture = channel.close();
        try {
            closeFuture.await(idleTimeout);
        } catch (InterruptedException e) {
            LOGGER.info("Disconnecting timeout.");
            throw e;
        }

        releaseExternalResources();

    }

    protected abstract SocketAddress getRemoteAddress();

    protected abstract SocketAddress getLocalAddress();

    protected abstract ChannelPipelineFactory getPipelineFactory(OftpletFactory oftpletFactory,
            Timer timer);

    protected abstract ChannelFactory createChannelFactory();

    public Runnable getDisconnectListener() {
        return disconnectListener;
    }

    public void setDisconnectListener(Runnable onDisconnect) {
        this.disconnectListener = onDisconnect;
    }

    protected Channel getChannel() {
        return channel;
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

    protected void releaseExternalResources() {
    	if (isManaged(timer)) {
    		LOGGER.trace("Releasing acquired timer: {}", timer);
    		timer.stop();
    	}
    }
}
