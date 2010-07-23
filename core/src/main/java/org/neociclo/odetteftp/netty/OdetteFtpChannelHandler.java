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
package org.neociclo.odetteftp.netty;

import static org.neociclo.odetteftp.ProtocolHandlerFactory.*;
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
import static org.neociclo.odetteftp.util.SessionHelper.*;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.Timer;
import org.neociclo.odetteftp.EntityType;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.ProtocolHandler;
import org.neociclo.odetteftp.oftplet.ChannelCallback;
import org.neociclo.odetteftp.oftplet.Oftplet;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.CommandIdentifier;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer;
import org.neociclo.odetteftp.protocol.EndSessionReason;
import org.neociclo.odetteftp.protocol.OdetteFtpExchangeBuffer;
import org.neociclo.odetteftp.util.OdetteFtpConstants;

/**
 * The {@link Timer} which was specified when the {@link ReadTimeoutHandler} is
 * created should be stopped manually by calling {@link #releaseExternalResources()}
 * or {@link Timer#stop()} when your application shuts down.
 *
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
@Sharable
public class OdetteFtpChannelHandler extends IdleStateAwareChannelHandler {

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
        int timeoutSeconds = session.getTimeout();
        ctx.getPipeline().addFirst("Timeout-HANDLER", new IdleStateHandler(timer, timeoutSeconds, timeoutSeconds, 0));

        // Add all accepted channels to the group so that they are closed
        // properly on shutdown. If the added channel is closed before shutdown,
        // it will be removed from the group automatically.
        if (channelGroup != null) {
            channelGroup.add(ctx.getChannel());
        }

        super.channelOpen(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {

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

        OdetteFtpSession session = ChannelContext.SESSION.get(ctx.getChannel());

        Oftplet oftplet = getSessionOftplet(session);
        oftplet.destroy();

        super.channelDisconnected(ctx, e);
    }

    public OftpletFactory getOftpletFactory() {
        return oftpletFactory;
    }

}
