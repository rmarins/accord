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
 */
@Sharable
public class OdetteFtpDecoder extends OneToOneDecoder {

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

        // returning message object
        OdetteFtpExchangeBuffer oftpExchangeBuffer;

        // incoming buffer raw data
        if (!(msg instanceof ChannelBuffer)) {
            return msg;
        }
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
        /*
         * The RFC5024 spec is not clear on which ESID format to return when
         * starting and OFTP2 session. This addition helps to keep it compatible
         * with other implementations.
         */
        else if (version.isEqualOrOlder(OdetteFtpVersion.OFTP_V20) && identifier == CommandIdentifier.ESID) {
        	version = OdetteFtpVersion.OFTP_V14;
        	commandFormat = CommandFormatHelper.resolveByVersion(version, identifier);
        	oftpExchangeBuffer = CommandExchangeBufferBuilder.create(commandFormat, in, channel.getConfig().getBufferFactory());
        }
        /* Same as above for ODETTE-FTP versions 1.2, 1.3 and 1.4. */
        else {
            oftpExchangeBuffer = CommandExchangeBufferBuilder.create(commandFormat, in, channel.getConfig().getBufferFactory());
        }

        return oftpExchangeBuffer;
    }

}
