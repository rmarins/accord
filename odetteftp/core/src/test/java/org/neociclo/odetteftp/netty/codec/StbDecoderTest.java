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

import static org.neociclo.odetteftp.netty.codec.StbConstants.*;
import static org.junit.Assert.*;
import static org.neociclo.odetteftp.protocol.CommandExchangeBuffer.DEFAULT_PROTOCOL_CHARSET;
import static org.jboss.netty.buffer.ChannelBuffers.*;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class StbDecoderTest {

    private DecoderEmbedder<ChannelBuffer> d;

    @Before
    public void setUp() throws Exception {
        d = new DecoderEmbedder<ChannelBuffer>(new StbDecoder());
    }

    @Test
    public void testDecodeStbWithCompleteFrame() throws Exception {

        ChannelBuffer setCreditCommandBuffer = wrappedBuffer("C  ".getBytes(DEFAULT_PROTOCOL_CHARSET));
        ChannelBuffer stbHeader = buffer(4);
        stbHeader.writeByte(STB_V1_NOFLAGS_HEADER);
        stbHeader.writeMedium(7);

        ChannelBuffer stb = wrappedBuffer(stbHeader, setCreditCommandBuffer);

        d.offer(stb);
        d.finish();

        ChannelBuffer decodedBuffer = d.poll();

        assertNotNull(decodedBuffer);

        setCreditCommandBuffer.discardReadBytes();
        assertEquals(setCreditCommandBuffer, decodedBuffer);

    }

    @Test
    public void testDecodeStbWithFragmentedFrame() throws Exception {

        ChannelBuffer expectedSetCreditCommandBuffer = wrappedBuffer("C  ".getBytes(DEFAULT_PROTOCOL_CHARSET));

        ChannelBuffer decodedBuffer = null;

        // put stb header
        d.offer(wrappedBuffer(new byte[] {STB_V1_NOFLAGS_HEADER, 0x00, 0x00, 0x07}));

        decodedBuffer = d.poll();
        assertNull(decodedBuffer);

        // put the command identifier
        d.offer(wrappedBuffer("C".getBytes(DEFAULT_PROTOCOL_CHARSET)));

        decodedBuffer = d.poll();
        assertNull(decodedBuffer);

        // put reserved field (first half)
        d.offer(wrappedBuffer(" ".getBytes(DEFAULT_PROTOCOL_CHARSET)));

        decodedBuffer = d.poll();
        assertNull(decodedBuffer);

        // put reserved field (second half)
        d.offer(wrappedBuffer(" ".getBytes(DEFAULT_PROTOCOL_CHARSET)));
        d.finish();

        // it's now completed
        decodedBuffer = d.poll();

        assertNotNull(decodedBuffer);

        assertEquals(expectedSetCreditCommandBuffer, decodedBuffer);

    }


    @Test
    public void testDecodeStbWithCancatenedFrames() throws Exception {

        // prepare the expected CDT exchange buffer
        ChannelBuffer setCreditCommandBuffer = wrappedBuffer("C  ".getBytes(DEFAULT_PROTOCOL_CHARSET));
        ChannelBuffer cdtHeader = buffer(4);
        cdtHeader.writeByte(STB_V1_NOFLAGS_HEADER);
        cdtHeader.writeMedium(7);

        // prepare sub-sequent DATA exchange buffer with 35 whitespaces and an end of record
        ChannelBuffer dataBuffer = wrappedBuffer(("D#                                   " + ((char) 0x80)).getBytes(DEFAULT_PROTOCOL_CHARSET));
        ChannelBuffer dataHeader = buffer(42);
        dataHeader.writeByte(STB_V1_NOFLAGS_HEADER);
        dataHeader.writeMedium(42);

        // assemble all frames in the input stream
        ChannelBuffer inStream = wrappedBuffer(cdtHeader, setCreditCommandBuffer, dataHeader, dataBuffer);

        d.offer(inStream);
        d.finish();

        ChannelBuffer decodedBuffer = d.poll();

        assertNotNull(decodedBuffer);

        setCreditCommandBuffer.discardReadBytes();
        assertEquals(setCreditCommandBuffer, decodedBuffer);


    }

}
