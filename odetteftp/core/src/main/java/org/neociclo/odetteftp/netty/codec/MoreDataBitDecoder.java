/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2010 Neociclo, http://www.neociclo.com
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
