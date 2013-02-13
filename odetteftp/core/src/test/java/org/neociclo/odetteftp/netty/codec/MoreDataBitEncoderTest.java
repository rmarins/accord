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
