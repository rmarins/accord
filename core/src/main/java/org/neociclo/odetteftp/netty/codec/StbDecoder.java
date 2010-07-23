/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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

import static org.neociclo.odetteftp.netty.codec.StbConstants.*;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
@Sharable
public class StbDecoder extends FrameDecoder {

    public StbDecoder() {
        super();
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel ch, ChannelBuffer buf) throws Exception {

        // Make sure if the length field was received.
        if (buf.readableBytes() < STB_MIN_BUFFER_SIZE) {
            // The length field was not received yet - return null.
            // This method will be invoked again when more packets are
            // received and appended to the buffer.
            return null;
        }

        // The length field is in the buffer.

        // Mark the current buffer position before reading the length field
        // because the whole frame might not be in the buffer yet.
        // We will reset the buffer position to the marked position if
        // there's not enough bytes in the buffer.
        buf.markReaderIndex();

        // check STB version avoiding flags
        byte versionAndFlags = buf.readByte();
        if ((versionAndFlags & STB_V1_NOFLAGS_HEADER) != STB_V1_NOFLAGS_HEADER) {
            throw new StbException("Format error. Invalid STB header version: " +
                    ((int) (versionAndFlags >> 4)));
        }

        // Read the length field.
        int length = (buf.readMedium() - STB_HEADER_SIZE);

        if (buf.readableBytes() < length) {
            buf.resetReaderIndex();
            return null;
        }

        // There's enough bytes in the buffer. Read it.
        ChannelBuffer frame = buf.readBytes(length);

        // Successfully decoded a frame. Return the decoded frame.
        return frame;
    }

}
