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

import static org.jboss.netty.buffer.ChannelBuffers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.neociclo.odetteftp.protocol.CommandExchangeBuffer.DEFAULT_PROTOCOL_CHARSET;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class MoreDataBitEncoderTest {

    private EncoderEmbedder<ChannelBuffer> e;

    @Before
    public void setUp() throws Exception {
        e = new EncoderEmbedder<ChannelBuffer>(new MoreDataBitEncoder());
    }

    @Test
    public void testEncodeSetCreditCommand() throws Exception {

        // MoreData Bit header + CDT command
        ChannelBuffer expectedBuffer = wrappedBuffer(
                new byte[] {
                        0x00, 0x03, // MoreData Bit frame header (length 3)
                        0x43, 0x20, 0x20 } // CDT exchange buffer 
                );

        ChannelBuffer cdt =  wrappedBuffer("C  ".getBytes(DEFAULT_PROTOCOL_CHARSET));
        e.offer(cdt);
        e.finish();

        ChannelBuffer encodedBuffer = e.poll();

        assertNotNull(encodedBuffer);
        assertEquals(expectedBuffer, encodedBuffer);

    }

}
