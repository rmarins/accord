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
 */
@Sharable
public class ProtocolLoggingHandler extends SimpleChannelHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolLoggingHandler.class);

    private Marker marker;

    public ProtocolLoggingHandler() {
	    this(null);
    }

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
            if (CommandIdentifier.DATA == oeb.getIdentifier()) {
                LOGGER.trace(marker, "          length: {}", oeb.getSize());         
            } else {
                traceCommand((CommandExchangeBuffer) oeb);
            }
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
        final CommandIdentifier commandIdentifier = oeb.getIdentifier();
        final String channelId = toHexString(ctx.getChannel().getId()).toUpperCase();
        final int size = oeb.getSize();
//        final String data = new String(oeb.getRawBuffer().array(), "US-ASCII");

        ChannelFutureListener messageSentListener = new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    if (LOGGER.isDebugEnabled(marker)) {
                        String cmd = paddCmdName(commandIdentifier.name());
                        LOGGER.debug(marker, "[{}]     ----------  {}  --------->", channelId, cmd);
                    }
                    if (LOGGER.isTraceEnabled(marker)) {
                    	if (CommandIdentifier.DATA == commandIdentifier) {
                            LOGGER.trace(marker, "          length: {}", size);
                            //LOGGER.trace(marker, "          data  :     {}", data);  
                        } else {
                            traceCommand((CommandExchangeBuffer) oeb);
                        }
                    }
                } else {
                    if (LOGGER.isTraceEnabled(marker) && future.getCause() != null) {
                    	LOGGER.trace(marker, "Failed to send message.", future.getCause());
                    }
                }
            }

        };

        e.getFuture().addListener(messageSentListener);

        super.writeRequested(ctx, e);
    }

    private void traceCommand(CommandExchangeBuffer cmd) {
        String[] fieldNames = cmd.getFieldNames();
        for (String name : fieldNames) {
            Object value = cmd.getAttribute(name);
            if (value instanceof byte[]) {
                LOGGER.trace(marker, "              {} = {}", padd(name, 9, false), toHexString((byte[]) value, 30));
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
