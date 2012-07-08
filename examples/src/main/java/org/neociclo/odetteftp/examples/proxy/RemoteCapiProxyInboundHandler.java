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

import static org.jboss.netty.buffer.ChannelBuffers.*;
import static org.neociclo.capi20.util.CapiBuffers.WORD_SIZE;
import static org.neociclo.capi20.util.CapiBuffers.readOctet;
import static java.lang.System.*;

import java.net.InetSocketAddress;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.neociclo.capi20.message.MessageType;

public class RemoteCapiProxyInboundHandler extends SimpleChannelUpstreamHandler {

	private final ClientSocketChannelFactory cf;
	private final String remoteHost;
	private final int remotePort;

	final RemoteCapiMessageProcessor[] messageProcessors;

	// This lock guards against the race condition that overrides the
	// OP_READ flag incorrectly.
	// See the related discussion: http://markmail.org/message/x7jc6mqx6ripynqf
	final Object trafficLock = new Object();

	private volatile Channel outboundChannel;

	public RemoteCapiProxyInboundHandler(ClientSocketChannelFactory cf, String remoteHost, int remotePort,
			RemoteCapiMessageProcessor[] messageProcessors) {
		super();
		this.cf = cf;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.messageProcessors = messageProcessors;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		// Suspend incoming traffic until connected to the remote host
		final Channel inboundChannel = e.getChannel();
		inboundChannel.setReadable(false);

		// Start the connection attempt
		ClientBootstrap cb = new ClientBootstrap(cf);
		cb.getPipeline().addLast("handler", new OutboundHandler(e.getChannel()));
		ChannelFuture f = cb.connect(new InetSocketAddress(remoteHost, remotePort));

		outboundChannel = f.getChannel();
		f.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					// Connection attempt succeeded:
					// Begin to accept incoming traffic
					inboundChannel.setReadable(true);
				} else {
					// Close the connection if the connection attempt has failed
					inboundChannel.close();
				}
			}
		});

	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		ChannelBuffer msg = filterMessage(this, ctx, e);
		synchronized (trafficLock) {
			outboundChannel.write(msg);
			// If outboundChannel is saturated, do not read until notified in
			// OutboundHandler.channelInterestChanged()
			if (!outboundChannel.isWritable()) {
				e.getChannel().setReadable(false);
			}
		}
	}

	@Override
	public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		// If inboundChannel is not saturated anymore, continue accepting
		// the incoming traffic from the outboundChannel
		synchronized (trafficLock) {
			if (e.getChannel().isWritable()) {
				outboundChannel.setReadable(true);
			}
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		if (outboundChannel != null) {
			closeOnFlush(outboundChannel);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		e.getCause().printStackTrace(err);
		closeOnFlush(e.getChannel());
	}

	public class OutboundHandler extends SimpleChannelUpstreamHandler {

		private final Channel inboundChannel;

		public OutboundHandler(Channel inboundChannel) {
			super();
			this.inboundChannel = inboundChannel;
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

			ChannelBuffer msg = filterMessage(this, ctx, e);
			synchronized (trafficLock) {
				inboundChannel.write(msg);
				// If inboundChannel is saturated, do not read until notified in
				// RemoteCapiProxyInboundHandler.channelInterestChanged()
				if (!inboundChannel.isWritable()) {
					e.getChannel().setReadable(false);
				}
			}
		}

		@Override
		public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			// If outboundChannel is not saturated anymore, continue accepting
			// the incoming traffic from the inboundChannel
			synchronized (trafficLock) {
				if (e.getChannel().isWritable()) {
					inboundChannel.setReadable(true);
				}
			}
		}

		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			closeOnFlush(inboundChannel);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
			e.getCause().printStackTrace(err);
			closeOnFlush(e.getChannel());
		}

	}

	/**
	 * Closes the specified channel after all queued write requests are flushed.
	 */
	static void closeOnFlush(Channel ch) {
		if (ch.isConnected()) {
			ch.write(EMPTY_BUFFER)
					.addListener(ChannelFutureListener.CLOSE);
		}
	}

	ChannelBuffer filterMessage(ChannelHandler source, ChannelHandlerContext ctx, MessageEvent e) {

		ChannelBuffer msg = (ChannelBuffer) e.getMessage();
		MessageType messageType = parseMessageType(msg);

		for (RemoteCapiMessageProcessor transformer : messageProcessors) {
			if (transformer.accept(source, messageType)) {
				return transformer.process(ctx, e);
			}
		}

		return msg;
	}

	private static MessageType parseMessageType(ChannelBuffer msg) {

		int startPos = msg.readerIndex();

        msg.skipBytes(6); // skip totalLength and appID header
        // fields
        byte command = readOctet(msg);
        byte subCommand = readOctet(msg);

//        out.printf("parseMessageType() :: command = %x : subcommand = %x :: [%s]\n", command, subCommand, ChannelBuffers.hexDump(msg));

        // rewind reader cursor to initial position
        msg.readerIndex(startPos);

        // select case upon messageType
        return MessageType.valueOf(command, subCommand);
	}

}
