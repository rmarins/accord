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

import static java.lang.System.*;
import static org.neociclo.capi20.message.MessageType.*;
import static org.jboss.netty.util.CharsetUtil.ISO_8859_1;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.neociclo.capi20.message.MessageType;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class RemoteCapiProxy {

	private final int localPort;
	private final String remoteHost;
	private final int remotePort;

	public RemoteCapiProxy(int localPort, String remoteHost, int remotePort) {
		super();
		this.localPort = localPort;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
	}

	public void run() {

		out.printf(
				"Proxying *:%d to %s:%d ...\n",
				localPort, remoteHost, remotePort);

		// Configure the bootstrap
		Executor executor = Executors.newCachedThreadPool();
		ServerBootstrap sb = new ServerBootstrap(
				new NioServerSocketChannelFactory(executor, executor));

		// Set up the event pipeline factory
		ClientSocketChannelFactory cf = new NioClientSocketChannelFactory(executor, executor);

		sb.setPipelineFactory(new RemoteCapiPipelineFactory(cf, remoteHost, remotePort, getCapiProcessors()));

		// Start up the server
		sb.bind(new InetSocketAddress(localPort));

	}

	private static RemoteCapiMessageProcessor[] getCapiProcessors() {

//		final SpecialLogicDecoder specialLogicDecoder = new SpecialLogicDecoder();
//		final SpecialLogicEncoder specialLogicEncoder = new SpecialLogicEncoder();

		RemoteCapiMessageProcessor inboundDataProcessor = new RemoteCapiMessageProcessor() {
			
			public ChannelBuffer process(ChannelHandlerContext ctx, MessageEvent e) {
				ChannelBuffer msg = (ChannelBuffer) e.getMessage();

				bufferReplaceBy(msg, "O094200005562851534TARNFO", "O01770000000000X0B5SHARED", ISO_8859_1);
				bufferReplaceBy(msg, "ODEXSVR ", "BABELWAY", ISO_8859_1);
				
				return msg;
			}
			
			public boolean accept(ChannelHandler source, MessageType messageType) {
				return ((messageType == DATA_B3_REQ) && (source instanceof RemoteCapiProxyInboundHandler));
			}
		};

		RemoteCapiMessageProcessor outboundDataProcessor = new RemoteCapiMessageProcessor() {
			
			public ChannelBuffer process(ChannelHandlerContext ctx, MessageEvent e) {
				ChannelBuffer msg = (ChannelBuffer) e.getMessage();

				bufferReplaceBy(msg, "O0013004468WABCOGRP000001", "DINET                    ", ISO_8859_1);
				bufferReplaceBy(msg, "WABCO   ", "NEOCICLO", ISO_8859_1);
				bufferReplaceBy(msg, "O01770000000000X0B5026134", "DINET                    ", ISO_8859_1);

				return msg;
			}
			
			public boolean accept(ChannelHandler source, MessageType messageType) {
//				out.printf("accept() ? %s :: %s\n", messageType, source);
				return ((messageType == DATA_B3_IND) && (source instanceof RemoteCapiProxyInboundHandler.OutboundHandler));
			}
		};

		return new RemoteCapiMessageProcessor[] { inboundDataProcessor, outboundDataProcessor };
	}

	public static void main(String[] args) {
		// Validate command line options
		if (args.length != 3) {
			err.println(
					"Usage: " + RemoteCapiProxy.class.getSimpleName() +
					" <local port> <remote host> <remote port>");
			exit(-1);
		}

		// Parse command line options
		int localPort = Integer.parseInt(args[0]);
		String remoteHost = args[1];
		int remotePort = Integer.parseInt(args[2]);

		new RemoteCapiProxy(localPort, remoteHost, remotePort).run();
	}

	public static ChannelBuffer bufferReplaceBy(ChannelBuffer buf, String match, String substitute, Charset charset) {

		if (match == null || "".equals(match)) {
			throw new NullPointerException("match");
		} else if (substitute == null || "".equals(substitute)) {
			throw new NullPointerException("newstr");
		}

//		out.printf("Replacing [%s] with [%s] in buffer [%s]\n", match, substitute, new String(buf.array(), ISO_8859_1));

		byte[] from = match.getBytes(charset);
		byte[] to = substitute.getBytes(charset);

		return bufferReplaceBy(buf, from, to);

	}

	public static ChannelBuffer bufferReplaceBy(ChannelBuffer buf, byte[] from, byte[] to) {

		int bytesToRead = buf.readableBytes();

		if (from.length != to.length) {
			throw new IllegalArgumentException("replaceBy params [from] and [to]'s length must match");
		} else if (from.length > bytesToRead) {
			throw new IllegalArgumentException("replace object length is greater than buffer's size");
		}

		int start = buf.readerIndex();
		int backup = buf.writerIndex();

		int pos = buf.indexOf(start, start + buf.readableBytes(), new ByteArrayIndexFinder(from));
		if (pos != -1) {
			buf.writerIndex(pos);
			buf.writeBytes(to, 0, to.length);
			buf.writerIndex(backup);
		}

		return buf;
	}


}
