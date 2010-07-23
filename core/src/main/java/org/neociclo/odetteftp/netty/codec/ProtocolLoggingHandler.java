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
package org.neociclo.odetteftp.netty.codec;

import static org.neociclo.odetteftp.util.OftpUtil.toHexString;
import static org.neociclo.odetteftp.util.BufferUtil.toHexString;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.CommandIdentifier;
import org.neociclo.odetteftp.protocol.OdetteFtpExchangeBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
@Sharable
public class ProtocolLoggingHandler extends SimpleChannelHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolLoggingHandler.class);

    private Marker marker;

    public ProtocolLoggingHandler(Marker marker) {
        super();
        this.marker = marker;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        if (!(e.getMessage() instanceof OdetteFtpExchangeBuffer)) {
            super.messageReceived(ctx, e);
            return;
        } else if (!LOGGER.isDebugEnabled(marker) && !LOGGER.isTraceEnabled(marker)) {
            super.messageReceived(ctx, e);
            return;
        }

        final OdetteFtpExchangeBuffer oeb = (OdetteFtpExchangeBuffer) e.getMessage();
        final String channelId = toHexString(ctx.getChannel().getId()).toUpperCase();
        
        if (LOGGER.isDebugEnabled(marker)) {
            String cmd = paddCmdName(oeb.getIdentifier().name());
            LOGGER.debug(marker, "[{}]     <---------  {}  ----------", channelId, cmd);
        }
        if (LOGGER.isTraceEnabled(marker)) {
            traceData(oeb);
        }

        super.messageReceived(ctx, e);

    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        if (!(e.getMessage() instanceof OdetteFtpExchangeBuffer)) {
            super.writeRequested(ctx, e);
            return;
        } else if (!LOGGER.isDebugEnabled(marker) && !LOGGER.isTraceEnabled(marker)) {
            super.writeRequested(ctx, e);
            return;
        }

        final OdetteFtpExchangeBuffer oeb = (OdetteFtpExchangeBuffer) e.getMessage();
        final String channelId = toHexString(ctx.getChannel().getId()).toUpperCase();

        ChannelFutureListener messageSentListener = new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    if (LOGGER.isDebugEnabled(marker)) {
                        String cmd = paddCmdName(oeb.getIdentifier().name());
                        LOGGER.debug(marker, "[{}]     ----------  {}  --------->", channelId, cmd);
                    }
                    if (LOGGER.isTraceEnabled(marker)) {
                        traceData(oeb);
                    }
                }
            }

        };

        e.getFuture().addListener(messageSentListener);

        super.writeRequested(ctx, e);
    }

    private void traceData(OdetteFtpExchangeBuffer oeb) throws Exception {
        if (CommandIdentifier.DATA == oeb.getIdentifier()) {
            LOGGER.trace(marker, "          length: {}", oeb.getSize());
        } else {
            traceCommand((CommandExchangeBuffer) oeb);
        }
    }

    private void traceCommand(CommandExchangeBuffer cmd) {
        String[] fieldNames = cmd.getFieldNames();
        for (String name : fieldNames) {
            Object value = cmd.getAttribute(name);
            if (value instanceof byte[]) {
                LOGGER.trace(marker, "              {} = {}", padd(name, 9, false), toHexString((byte[]) value, 10));
            } else {
                LOGGER.trace(marker, "              {} = {}", padd(name, 9, false), value);
            }
        }
    }

    private static String paddCmdName(String name) {
        switch (name.length()) {
        case 3:
            return name + " ";
        case 2:
            return " " + name + " ";
        }
        return name;
    }

    private static String padd(String text, int length, boolean left) {

        if (text.length() < length) {
            String concat = "";
            int diff = length - text.length();
            for (int i=0; i<diff; i++) {
                concat += ' ';
            }
            if (left) {
                return (concat + text);
            } else {
                return (text + concat);
            }
        }

        return text;
    }
}
