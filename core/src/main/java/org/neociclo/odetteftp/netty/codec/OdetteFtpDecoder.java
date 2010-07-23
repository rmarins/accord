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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransportType;
import org.neociclo.odetteftp.netty.ChannelContext;
import org.neociclo.odetteftp.protocol.CommandFormat;
import org.neociclo.odetteftp.protocol.CommandIdentifier;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer;
import org.neociclo.odetteftp.protocol.OdetteFtpExchangeBuffer;
import org.neociclo.odetteftp.util.CommandFormatHelper;

/**
 * Decode incoming buffer raw data into OdetteFtpExchangeBuffer objects.
 * <p>
 * This implementation will work with {@link TransportType#TCPIP} and
 * {@link TransportType#X25_MBGW}, and other underlying communication channels
 * might not work. Pure ISDN and X25 connection like may require a version of
 * <code>OdetteFtpDecoder</code> specialized from the {@link FrameDecoder}.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
@Sharable
public class OdetteFtpDecoder extends OneToOneDecoder {

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

        // returning message object
        OdetteFtpExchangeBuffer oftpExchangeBuffer;

        // incoming buffer raw data
        ChannelBuffer in = (ChannelBuffer) msg;

        /* Identify the command identifier in ODETTE-FTP Exchange Buffer. */
        CommandIdentifier identifier = null;
// TODO solve the problem below via CommandExchangeBuffer resillence
//        try {
            identifier = CommandIdentifier.parse((char) in.readByte());
//        } catch (CommandNotRecognisedException cnre) {
//            if ('\r' == cnre.getCode() || '\n' == cnre.getCode()) {
//                // ignore - because a session started in OFTPv2 might receive an
//                // OFTP v1.x ESID command format, which doesn't have some params
//                // like Reason Text Length and Reason Text; otherwise, will throw
//                // the exception on parsing next byte as CommandIdentifier.
//                return null;
//            }
//        }

        /*
         * Retrieve the corresponding CommandFormat instance of the discovered
         * identifier, by using the relative protocol version.
         */
        OdetteFtpSession session = ChannelContext.SESSION.get(ctx.getChannel());
        OdetteFtpVersion version = session.getVersion();
        CommandFormat commandFormat = CommandFormatHelper.resolveByVersion(version, identifier);

        /*
         * Check if Data Exchange Buffer is complete and build its instance
         * using the IoBuffer data.
         */
        if (identifier == CommandIdentifier.DATA) {
            oftpExchangeBuffer = new DataExchangeBuffer(in.toByteBuffer());
        }
        /* Same as above for ODETTE-FTP versions 1.2, 1.3 and 1.4. */
        else {
            oftpExchangeBuffer = CommandExchangeBufferBuilder.create(commandFormat, in);
        }

        return oftpExchangeBuffer;
    }

}
