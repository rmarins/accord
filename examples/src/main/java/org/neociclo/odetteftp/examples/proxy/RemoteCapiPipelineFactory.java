/**
 * Neociclo Accord, Open Source B2B Integration Suite
 * Copyright (C) 2005-2012 Neociclo, http://www.neociclo.com
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
package org.neociclo.odetteftp.examples.proxy;

import static org.jboss.netty.channel.Channels.*;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class RemoteCapiPipelineFactory implements ChannelPipelineFactory {

	private ClientSocketChannelFactory cf;
	private String remoteHost;
	private int remotePort;
	private RemoteCapiMessageProcessor[] messageProcessors;

	public RemoteCapiPipelineFactory(ClientSocketChannelFactory cf, String remoteHost, int remotePort) {
		this(cf, remoteHost, remotePort, null);
	}

	public RemoteCapiPipelineFactory(ClientSocketChannelFactory cf, String remoteHost, int remotePort, RemoteCapiMessageProcessor[] messageProcessors) {
		this.cf = cf;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.messageProcessors = messageProcessors;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline p = pipeline();
		p.addLast("handler", new RemoteCapiProxyInboundHandler(cf, remoteHost, remotePort, messageProcessors));
		return p;
	}

}
