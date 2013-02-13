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

import static org.neociclo.odetteftp.netty.codec.MoreDataBitConstants.*;

import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
@Sharable
public class MoreDataBitDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * Create decoder instance based on {@link LengthFieldBasedFrameDecoder}.
     * Length field contains the buffer data size only. Use the following
     * settings:
     * <pre>
     * lengthFieldOffset   = 0
     * lengthFieldLength   = 2
     * lengthAdjustment    = 0
     * <b>initialBytesToStrip</b> = <b>2</b> (= the length of the Length field)
     *
     * BEFORE DECODE (14 bytes)         AFTER DECODE (12 bytes)
     * +--------+----------------+      +----------------+
     * | Length | Actual Content |----->| Actual Content |
     * | 0x000C | "HELLO, WORLD" |      | "HELLO, WORLD" |
     * +--------+----------------+      +----------------+
     * </pre>
     */
    public MoreDataBitDecoder() {
        super((int) 0xffff, // max short value (unsigned)
                0, // length field offset
                MORE_DATA_BIT_LENGTH_FIELD_SIZE, // size of length field
                0, // no bytes adjusment
                MORE_DATA_BIT_LENGTH_FIELD_SIZE // bytes to strip from buffer
        );
    }

}
