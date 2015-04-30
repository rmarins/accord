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
package org.neociclo.odetteftp.netty;

import static org.neociclo.odetteftp.ProtocolHandlerFactory.getProtocolHandlerByVersion;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.AUCH;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.AURP;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.CD;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.CDT;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.DATA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EERP;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EFID;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EFNA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EFPA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.ESID;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.NERP;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.RTR;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SECD;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SFID;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SFNA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SFPA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SSID;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SSRM;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_OFTP_ENTITY_TYPE;
import static org.neociclo.odetteftp.util.SessionHelper.getSessionOftplet;
import static org.neociclo.odetteftp.util.SessionHelper.setSessionOftplet;

import java.util.concurrent.TimeUnit;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.Timer;
import org.neociclo.odetteftp.*;
import org.neociclo.odetteftp.netty.codec.SpecialLogicDecoder;
import org.neociclo.odetteftp.netty.codec.SpecialLogicEncoder;
import org.neociclo.odetteftp.oftplet.ChannelCallback;
import org.neociclo.odetteftp.oftplet.Oftplet;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.CommandIdentifier;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer;
import org.neociclo.odetteftp.protocol.EndSessionReason;
import org.neociclo.odetteftp.protocol.OdetteFtpExchangeBuffer;
import org.neociclo.odetteftp.util.OdetteFtpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Timer} which was specified when the {@link ReadTimeoutHandler} is
 * created should be stopped manually by calling {@link #releaseExternalResources()}
 * or {@link Timer#stop()} when your application shuts down.
 *
 * @author Rafael Marins
 */
@Sharable
public class OdetteFtpChannelHandler extends IdleStateAwareChannelHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OdetteFtpChannelHandler.class);

    /**
     * Implement interface to provide in OdetteFtpSession to allow writing
     * messages in the communication channel.
     */
    public static class ChannelCallbackHandler implements ChannelCallback {

        private Channel channel;

        public ChannelCallbackHandler(Channel channel) {
            super();
            this.channel = channel;
        }

        public void write(Object message, final Runnable execOnComplete) {

            // write to the channel
            ChannelFuture writeFuture = channel.write(message);

            // schedule write future listener
            if (execOnComplete != null) {
                writeFuture.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            execOnComplete.run();
                        }
                    }
                });
            }

        }

        public void close() {
            if (channel.isConnected()) {
                channel.write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }

        public void closeImmediately() {
            if (channel.isConnected()) {
                channel.close();
            }
        }

    }

    /** Determine the ODETTE FTP entity type: Initiator or Responder. */
    private EntityType entityType;

    private OftpletFactory oftpletFactory;

    private Timer timer;

    private ChannelGroup channelGroup;

    /**
     * Default constructor. Handler creates ODETTE FTP entity starting using
     * default entity type and version values.
     * @param channelGroup 
     * 
     * @see OdetteFtpConstants#DEFAULT_OFTP_ENTITY_TYPE
     * @see OdetteFtpConstants#DEFAULT_OFTP_VERSION
     */
    public OdetteFtpChannelHandler(OftpletFactory oftpletFactory, Timer timer, ChannelGroup channelGroup) {
        this(DEFAULT_OFTP_ENTITY_TYPE, oftpletFactory, timer, channelGroup);
    }

    /**
     * Let create the handler using ODETTE FTP entity type (Initiator or
     * Responder) and protocol version of your choice.
     * 
     * @param entityType
     * @param channelGroup 
     * @param version
     */
    public OdetteFtpChannelHandler(EntityType entityType, OftpletFactory oftpletFactory, Timer timer, ChannelGroup channelGroup) {
        super();
        if (timer == null) {
            throw new NullPointerException("timer");
        }
        this.entityType = entityType;
        this.oftpletFactory = oftpletFactory;
        this.timer = timer;
        this.channelGroup = channelGroup;
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    	LOGGER.debug("Channel open");
        OdetteFtpSession session = new OdetteFtpSession(entityType);
        ChannelContext.SESSION.set(e.getChannel(), session);

        ChannelCallbackHandler channelWriter = new ChannelCallbackHandler(e.getChannel()); 
        session.setChannelCallback(channelWriter);

        /* create new instance of the Oftplet implementation */
        Oftplet oftplet = getOftpletFactory().createProvider();
        setSessionOftplet(session, oftplet);

        /* let the Oftplet implementation adjust the parameters */
        oftplet.init(session);

        // configure channel idle based on configured session timeout
		long timeoutInMillis = session.getTimeout();
		ctx.getPipeline().addFirst("Timeout-HANDLER",
				new IdleStateHandler(timer, timeoutInMillis, timeoutInMillis, 0, TimeUnit.MILLISECONDS));

        // Add all accepted channels to the group so that they are closed
        // properly on shutdown. If the added channel is closed before shutdown,
        // it will be removed from the group automatically.
        if (channelGroup != null) {
            channelGroup.add(e.getChannel());
        }

        super.channelOpen(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    	LOGGER.debug("Channel connected");
        OdetteFtpSession session = ChannelContext.SESSION.get(ctx.getChannel());

        /* Get handler implementation for the correct protocol version. */
        OdetteFtpVersion version = session.getVersion();
        ProtocolHandler handler = getProtocolHandlerByVersion(version);

        /*
         * Delegate the session opened processing to the correct handler
         * implementation version.
         */
        handler.sessionConnected(session);

        super.channelConnected(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        /* Cast received message to Odette Exchange Buffer object. */
    	if (!(e.getMessage() instanceof OdetteFtpExchangeBuffer)) {
    		ctx.sendUpstream(e);
    		return;
    	}
        OdetteFtpExchangeBuffer message = (OdetteFtpExchangeBuffer) e.getMessage();

        OdetteFtpSession session = ChannelContext.SESSION.get(ctx.getChannel());

        /* Get handler implementation for the correct protocol version. */
        OdetteFtpVersion version = session.getVersion();
        ProtocolHandler handler = getProtocolHandlerByVersion(version);

        /* Unwrap the command identifier from the Odette Ftp Exchange Buffer. */
        CommandIdentifier identifier = message.getIdentifier();

        /*
         * Record in ODETTE FTP context the identifier of last received command
         * exchange buffer.
         */
        session.setLastCommandReceived(identifier);

        /**
         * ODETTE FTP peers communicate by sending and receiving messages in
         * Exchange Buffers via the Network Service. Each Exchange Buffer
         * contains one of the following commands below.
         * <p>
         * Delegate the command received processing to the correct version
         * handler implementation.
         */

        // Data Transfer buffer
        if (DATA == identifier) {
            handler.dataBufferReceived(session, (DataExchangeBuffer) message);
        }
        // Set Credit command
        else if (CDT == identifier) {
            handler.setCreditReceived(session, (CommandExchangeBuffer) message);
        }
        // Change Direction command
        else if (CD == identifier) {
            handler.changeDirectionReceived(session);
        }
        // Ready to Receive command
        else if (RTR == identifier) {
            handler.readyToReceiveReceived(session);
        }
        // Start File command
        else if (SFID == identifier) {
            handler.startFileReceived(session, (CommandExchangeBuffer) message);
        }
        // Start File Positive Answer command
        else if (SFPA == identifier) {
            handler.startFilePositiveAnswerReceived(session, (CommandExchangeBuffer) message);
        }
        // End File command
        else if (EFID == identifier) {
            handler.endFileReceived(session, (CommandExchangeBuffer) message);
        }
        // End File Positive Answer command
        else if (EFPA == identifier) {
            handler.endFilePositiveAnswerReceived(session, (CommandExchangeBuffer) message);
        }
        // End-to-End Response command
        else if (EERP == identifier) {
            handler.endToEndResponseReceived(session, (CommandExchangeBuffer) message);
        }
        // Start Session command
        else if (SSID == identifier) {
            handler.startSessionReceived(session, (CommandExchangeBuffer) message);

            setUpSpecialLogicWhenAgreed(session, ctx);

            ProtocolHandler h = getProtocolHandlerByVersion(session.getVersion());
            h.afterStartSession(session);
        }
        // Authentication Challenge command
        else if (AUCH == identifier) {
            handler.authenticationChallengeReceived(session, (CommandExchangeBuffer) message);
        }
        // Authentication Response command
        else if (AURP == identifier) {
            handler.authenticationResponseReceived(session, (CommandExchangeBuffer) message);
        }
        // Security Change Direction command
        else if (SECD == identifier) {
            handler.securityChangeDirectionReceived(session, (CommandExchangeBuffer) message);
        }
        // Start Session Ready Message command
        else if (SSRM == identifier) {
            handler.readyMessageReceived(session);
        }
        // End Session command
        else if (ESID == identifier) {
            handler.endSessionReceived(session, (CommandExchangeBuffer) message);
        }
        // Start File Negative Answer command
        else if (SFNA == identifier) {
            handler.startFileNegativeAnswerReceived(session, (CommandExchangeBuffer) message);
        }
        // End File Negative Answer command
        else if (EFNA == identifier) {
            handler.endFileNegativeAnswerReceived(session, (CommandExchangeBuffer) message);
        }
        // Negative End Response
        else if (NERP == identifier) {
            handler.negativeEndReponseReceived(session, (CommandExchangeBuffer) message);
        }

        super.messageReceived(ctx, e);
    }

	private void setUpSpecialLogicWhenAgreed(OdetteFtpSession session, ChannelHandlerContext ctx) {
		if (session.hasSpecialLogic()) {
			ChannelPipeline p = ctx.getPipeline();

			String baseHandlerName = "OdetteExchangeBuffer-DECODER";

			p.addBefore(baseHandlerName, "SpecialLogic-ENCODER", new SpecialLogicEncoder());
			p.addBefore(baseHandlerName, "SpecialLogic-DECODER", new SpecialLogicDecoder());

		}
	}

	@Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        if (!(e.getMessage() instanceof OdetteFtpExchangeBuffer)) {
            super.writeRequested(ctx, e);
            return;
        }

        OdetteFtpSession session = ChannelContext.SESSION.get(ctx.getChannel());
        OdetteFtpExchangeBuffer message = (OdetteFtpExchangeBuffer) e.getMessage();

        // set last command sent in session attribute
        session.setLastCommandSent(message.getIdentifier());

        super.writeRequested(ctx, e);
    }

    /**
     * This method is called whenever the communication data flow is idle and
     * the session timeout limit is over.
     */
    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
    	LOGGER.debug("Channel timed out");
        OdetteFtpSession session = ChannelContext.SESSION.get(ctx.getChannel());

        // session is terminated by timeout
        OdetteFtpVersion version = session.getVersion();
        ProtocolHandler handler = getProtocolHandlerByVersion(version);

        // TODO perhaps we may specify the timeout period in release message
        handler.abort(session, EndSessionReason.TIME_OUT, null);

        super.channelIdle(ctx, e);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    	LOGGER.debug("Channel disconnected " + e.getState());

        super.channelDisconnected(ctx, e);
    }

    public OftpletFactory getOftpletFactory() {
        return oftpletFactory;
    }

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		LOGGER.debug("Channel exception " + e.getCause().getClass().getName());
		
		OdetteFtpSession session = ChannelContext.SESSION.get(ctx.getChannel());
		if (session != null) {
			Oftplet oftplet = getSessionOftplet(session);
			oftplet.onExceptionCaught(e.getCause());
			if (e.getCause() instanceof OdetteFtpException) {
				session.close();
			} else {
				session.closeImmediately();
			}
		} else {
			// channel already disconnected
		}
	}
	
	@Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		LOGGER.debug("Channel closed");
		OdetteFtpSession session = ChannelContext.SESSION.remove(ctx.getChannel());

		if (session != null) {
			Oftplet oftplet = getSessionOftplet(session);
			oftplet.destroy();
			session.close();
		} else {
			// channel already disconnected
		}

        super.channelClosed(ctx, e);
    }
}
