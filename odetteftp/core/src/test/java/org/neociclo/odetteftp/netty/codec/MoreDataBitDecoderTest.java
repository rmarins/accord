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

import static org.junit.Assert.*;
import static org.jboss.netty.buffer.ChannelBuffers.*;
import static org.neociclo.odetteftp.protocol.CommandExchangeBuffer.DEFAULT_PROTOCOL_CHARSET;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class MoreDataBitDecoderTest {

    private DecoderEmbedder<ChannelBuffer> d;

    @Before
    public void setUp() throws Exception {
        d = new DecoderEmbedder<ChannelBuffer>(new MoreDataBitDecoder());
    }

    @Test
    public void testDecodeMoreDataBitCompleteFrame() throws Exception {

        // MoreData Bit header + CDT command
        ChannelBuffer inStream = wrappedBuffer(
                new byte[] {
                        0x00, 0x03,         // MoreData Bit 
                        0x43, 0x20, 0x20 }  // CDT exchange buffer 
                );

        ChannelBuffer expectedSetCreditCommand = wrappedBuffer(
                new byte[] {
                        0x43, 0x20, 0x20 }  // CDT exchange buffer
                );

        d.offer(inStream);
        d.finish();

        ChannelBuffer decodedBuffer = d.poll();

        assertNotNull(decodedBuffer);

        assertEquals(expectedSetCreditCommand, decodedBuffer);

    }

    @Test
    public void testDecodeMoreDataBitWithFragmentedFrame() throws Exception {

        // MoreData Bit header + CDT command
        ChannelBuffer inStream = wrappedBuffer(
                new byte[] {
                        0x00, 0x03,         // MoreData Bit 
                        0x43, 0x20, 0x20 }  // CDT exchange buffer 
        );

        ChannelBuffer expectedSetCreditCommand = wrappedBuffer(
                new byte[] {
                        0x43, 0x20, 0x20 }  // CDT exchange buffer
                );

        ChannelBuffer fragment = buffer(1);
        ChannelBuffer decodedBuffer = null;

        do {

            decodedBuffer = d.poll();
            assertNull(decodedBuffer);

            fragment.clear();
            inStream.readBytes(fragment);

            d.offer(fragment);

        } while (inStream.readable());

        d.finish();
        decodedBuffer = d.poll();

        assertNotNull(decodedBuffer);

        assertEquals(expectedSetCreditCommand, decodedBuffer);

    }

    @Test
    public void testDecodeMoreDataBitWithCancatenedFrames() throws Exception {

        // prepare the expected CDT exchange buffer
        ChannelBuffer setCreditCommandBuffer = wrappedBuffer("C  ".getBytes(DEFAULT_PROTOCOL_CHARSET));
        ChannelBuffer cdtHeader = buffer(2);
        cdtHeader.writeShort(3);

        // prepare sub-sequent DATA exchange buffer with 35 whitespaces and an end of record
        ChannelBuffer dataBuffer = wrappedBuffer(("D#                                   " + ((char) 0x80)).getBytes(DEFAULT_PROTOCOL_CHARSET));
        ChannelBuffer dataHeader = buffer(38);
        dataHeader.writeMedium(38);

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
