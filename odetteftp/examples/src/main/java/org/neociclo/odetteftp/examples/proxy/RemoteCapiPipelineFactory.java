/**
 * Neociclo Accord, Open Source B2B Integration Suite
 * Copyright (C) 2005-2012 Neociclo, http://www.neociclo.com
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
