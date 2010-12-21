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

import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.ServerChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.util.Timer;
import org.neociclo.odetteftp.EntityType;
import org.neociclo.odetteftp.TransportType;
import org.neociclo.odetteftp.netty.OdetteFtpPipelineFactory;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.util.ExecutorUtil;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class TcpServer extends Server {

    private InetSocketAddress localAddress;
    private SSLEngine sslEngine;
    private Boolean startTls;

    private Executor bossExecutor;
    private Executor workerExecutor;

    public TcpServer(InetSocketAddress localAddress, OftpletFactory oftpletFactory) {
        this(localAddress, null, oftpletFactory);
    }

    public TcpServer(InetSocketAddress localAddress, SSLEngine sslEngine, OftpletFactory oftpletFactory) {
        this(localAddress, sslEngine, null, oftpletFactory);
    }

    public TcpServer(InetSocketAddress localAddress, SSLEngine sslEngine, Boolean startTls, OftpletFactory oftpletFactory) {
        super(oftpletFactory);
        this.sslEngine = sslEngine;
        this.startTls = startTls;
        this.localAddress = localAddress;
    }

    @Override
    protected ServerChannelFactory createServerChannelFactory() {

    	if (bossExecutor == null) {
    		bossExecutor = Executors.newCachedThreadPool();
    		setManaged(bossExecutor);
    	}

    	if (workerExecutor == null) {
    		workerExecutor = Executors.newCachedThreadPool();
    		setManaged(workerExecutor);
    	}

    	ServerChannelFactory factory = new NioServerSocketChannelFactory(bossExecutor, workerExecutor);
        return factory;
    }

    @Override
    protected SocketAddress getAddress() {
        return localAddress;
    }

    @Override
    protected OdetteFtpPipelineFactory getPipelineFactory(OftpletFactory oftpletFactory, Timer timer,
            ChannelGroup channelGroup) {

        SslHandler sslHandler = null;
        if (sslEngine != null) {
            if (startTls == null) {
                sslHandler = new SslHandler(sslEngine);
            } else {
                sslHandler = new SslHandler(sslEngine, startTls.booleanValue());
            }
        }

        OdetteFtpPipelineFactory pipelineFactory = new OdetteFtpPipelineFactory(EntityType.RESPONDER, oftpletFactory,
                timer, getTransportType(), sslHandler, channelGroup);

        if (isLoggingDisabled()) {
        	pipelineFactory.disableLogging();
        }

        return pipelineFactory;
    }

	public TransportType getTransportType() {
		return TransportType.TCPIP;
	}

	public Executor getBossExecutor() {
		return bossExecutor;
	}

	public void setBossExecutor(Executor bossExecutor) {
		this.bossExecutor = bossExecutor;
	}

	public Executor getWorkerExecutor() {
		return workerExecutor;
	}

	public void setWorkerExecutor(Executor workerExecutor) {
		this.workerExecutor = workerExecutor;
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
}
