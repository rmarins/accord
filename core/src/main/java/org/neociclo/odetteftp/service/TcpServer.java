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

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class TcpServer extends Server {

	private static final TransportType TCPIP_TRANSPORT_TYPE = TransportType.TCPIP;

    private InetSocketAddress localAddress;
    private SSLEngine sslEngine;
    private Boolean startTls;

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
        ServerChannelFactory factory = new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
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
        return pipelineFactory;
    }

	public TransportType getTransportType() {
		return TCPIP_TRANSPORT_TYPE;
	}
}
