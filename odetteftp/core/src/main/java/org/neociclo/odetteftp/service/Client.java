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

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
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
 */
public abstract class Client extends BaseService {

    public static final int DEFAULT_CONNECT_TIMEOUT = 45000;

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private OftpletFactory oftpletFactory;
    private long idleTimeout = TimeUnit.SECONDS.toMillis(90);

    private Channel channel;
    private Timer timer;

    private boolean connected;

    protected Runnable disconnectListener;

    private SocketAddress remoteAddress;

    private SocketAddress localAddress;

    private ChannelFactory channelFactory;

    private Map<String, Object> clientOptions = new HashMap<String, Object>();

    public Client() {
        super();
    }

    public Client(OftpletFactory oftpletFactory) {
        super();
        setOftpletFactory(oftpletFactory);
    }

    public synchronized void connect(SocketAddress remoteAddress) throws Exception {
        connect(remoteAddress, null, false);
    }

    public synchronized void connect(SocketAddress remoteAddress, boolean await) throws Exception {
        connect(remoteAddress, null, await);
    }

    public synchronized void connect(SocketAddress remoteAddress, SocketAddress localAddress, boolean await)
            throws Exception {

        ChannelFactory factory = getChannelFactory();

        if (timer == null) {
            timer = new HashedWheelTimer();
            setManaged(timer);
            LOGGER.trace("Managed timer acquired: {}", timer);
        }

        ChannelPipelineFactory pipelineFactory = getPipelineFactory(oftpletFactory, timer);

        ClientBootstrap bootstrap = new ClientBootstrap(factory);

        if (!clientOptions.containsKey("connectTimeoutMillis")) {
            clientOptions.put("connectTimeoutMillis", DEFAULT_CONNECT_TIMEOUT);
        }

        bootstrap.setOptions(clientOptions);
        bootstrap.setPipelineFactory(pipelineFactory);

        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;

        LOGGER.info("Connecting to ODETTE-FTP service on {}...", remoteAddress);

        ChannelFuture connectFuture = bootstrap.connect(remoteAddress, localAddress);

        ChannelFutureListener setConnectedOnOpen = new ChannelFutureListener() {
            public void operationComplete(ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                    setConnected();
                    LOGGER.info("Connected.");
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
                // setDisconnected();
                LOGGER.debug("Call DisconnectListener");
                if (getDisconnectListener() != null) {
                    getDisconnectListener().run();
                }
            }
        };
        closeFuture.addListener(setDisconnectedOnClose);

        // need await disconnect
        if (await) {
            awaitDisconnect();
        } else {
            closeOnDisconnect();
        }

    }

    /**
     * Close on disconnect.
     */
    public synchronized void closeOnDisconnect() {
        Thread closer = new Thread(new Runnable() {

            /**
             * @see java.lang.Runnable#run()
             */
            @Override
            public void run() {
                awaitDisconnect();
            }
        }, "ShutdownOnDisconnect");

        closer.start();
    }

    /**
     * Await disconnect.
     */
    public synchronized void awaitDisconnect() {
        if (channel == null) {
            throw new IllegalStateException("The connect() method were not invoked.");
        }
        LOGGER.info("Await disconnect ...");
        ChannelFuture closeFuture = channel.getCloseFuture();
        closeFuture.awaitUninterruptibly();
        LOGGER.info("Release external resources");
        getChannelFactory().releaseExternalResources();
        releaseExternalResources();
        setDisconnected();
        LOGGER.info("Disconntected.");
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

    public OftpletFactory getOftpletFactory() {
        return oftpletFactory;
    }

    public void setOftpletFactory(OftpletFactory oftpletFactory) {
        this.oftpletFactory = oftpletFactory;
    }

    protected SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    protected SocketAddress getLocalAddress() {
        Channel c = getChannel();
        if (c == null) {
            return localAddress;
        }
        return c.getLocalAddress();
    }

    protected abstract ChannelPipelineFactory getPipelineFactory(OftpletFactory oftpletFactory,
            Timer timer);

    public ChannelFactory getChannelFactory() {
        return channelFactory;
    }

    public void setChannelFactory(ChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }

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

    public void setOption(String key, Object value) {
        clientOptions.put(key, value);
    }

    public void setOptions(Map<String, Object> clientOptions) {
        this.clientOptions = clientOptions;
    }

    public Map<String, Object> getOptions() {
        return clientOptions;
    }

    public Object getOption(String key) {
        return clientOptions.get(key);
    }

    protected void releaseExternalResources() {
        if (isManaged(timer)) {
            LOGGER.trace("Releasing acquired timer: {}", timer);
            timer.stop();
        }
    }
}
