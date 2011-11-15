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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.util.Timer;
import org.neociclo.odetteftp.EntityType;
import org.neociclo.odetteftp.TransportType;
import org.neociclo.odetteftp.netty.OdetteFtpPipelineFactory;
import org.neociclo.odetteftp.netty.SslHandlerFactory;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.util.ExecutorUtil;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class TcpClient extends Client {

    private static final int DEFAULT_NON_SSL_PORT = 3305;
	private static final int DEFAULT_SSL_PORT = 6619;

	private Executor bossExecutor;
	private Executor workerExecutor;

    private InetSocketAddress remoteAddress;
    private SSLContext sslContext;

    public TcpClient(String host, OftpletFactory oftpletFactory) {
        super(oftpletFactory);

        this.remoteAddress = new InetSocketAddress(host, DEFAULT_NON_SSL_PORT);
    }

    public TcpClient(String host, SSLContext sslContext, OftpletFactory oftpletFactory) {
        super(oftpletFactory);

        this.remoteAddress = new InetSocketAddress(host, DEFAULT_SSL_PORT);
        this.sslContext = sslContext;
    }

    public TcpClient(String host, int port, OftpletFactory oftpletFactory) {
        this(host, port, null, oftpletFactory);
    }

    public TcpClient(String host, int port, SSLContext sslContext, OftpletFactory oftpletFactory) {
        this(new InetSocketAddress(host, port), sslContext, oftpletFactory);
    }

    public TcpClient(InetSocketAddress remoteAddress, OftpletFactory oftpletFactory) {
        this(remoteAddress, null, oftpletFactory);
    }

    public TcpClient(InetSocketAddress remoteAddress, SSLContext sslContext, OftpletFactory oftpletFactory) {
        super(oftpletFactory);

        this.remoteAddress = remoteAddress;
        this.sslContext = sslContext;
    }

	@Override
    protected ChannelFactory createChannelFactory() {

        if (bossExecutor == null) {
            bossExecutor = Executors.newCachedThreadPool();
            setManaged(bossExecutor);
        }

        if (workerExecutor == null) {
        	workerExecutor = Executors.newCachedThreadPool();
            setManaged(workerExecutor);
        }

        ChannelFactory factory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor);
        return factory;
    }

    @Override
    protected SocketAddress getLocalAddress() {
        Channel c = getChannel();
        if (c == null) {
            return null;
        }
        return c.getLocalAddress();
    }

    @Override
    protected ChannelPipelineFactory getPipelineFactory(OftpletFactory oftpletFactory, Timer timer) {

        SslHandlerFactory sslHandlerFactory = new SslHandlerFactory() {
			public SslHandler createSslHandler() {
				SslHandler sslHandler = null;
		        if (sslContext != null) {
		        	SSLEngine engine = sslContext.createSSLEngine();
		    		engine.setUseClientMode(true);
		    		engine.setEnableSessionCreation(true);
		            sslHandler = new SslHandler(engine);
		        }
				return sslHandler;
			}
		};

        OdetteFtpPipelineFactory pipelineFactory = new OdetteFtpPipelineFactory(EntityType.INITIATOR, oftpletFactory,
                timer, getTransportType(), sslHandlerFactory, null);

        if (isLoggingDisabled()) {
        	pipelineFactory.disableLogging();
        }

        return pipelineFactory;
    }

    @Override
    protected SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public Executor getBossExecutor() {
        return bossExecutor;
    }

	/**
	 * The Executor which was specified should be terminated manually by calling
	 * {@link ExecutorUtil#terminate(Executor...)} when your application shuts
	 * down.
	 * 
	 * @param executor
	 */
	public void setWorkerExecutor(Executor workerExecutor) {
        if (getChannel() != null) {
            throw new IllegalStateException("Channel already created. Executor must be set before connect.");
        }

		this.workerExecutor = workerExecutor;
	}

	public Executor getWorkerExecutor() {
		return workerExecutor;
	}

	/**
	 * The Executor which was specified should be terminated manually by calling
	 * {@link ExecutorUtil#terminate(Executor...)} when your application shuts
	 * down.
	 * 
	 * @param executor
	 */
    public void setBossExecutor(Executor executor) {
        if (getChannel() != null) {
            throw new IllegalStateException("Channel already created. Executor must be set before connect.");
        }

        this.bossExecutor = executor;
    }

    @Override
    protected void releaseExternalResources() {
    	if (isManaged(bossExecutor)) {
    		ExecutorUtil.terminate(bossExecutor);
    	}
    	if (isManaged(workerExecutor)) {
    		ExecutorUtil.terminate(workerExecutor);
    	}
    	super.releaseExternalResources();
    }

	protected TransportType getTransportType() {
		return TransportType.TCPIP;
	}
}
