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
//        // MoreData Bit header + CDT command
//        ChannelBuffer inStream = wrappedBuffer(
//                new byte[] {
//                        0x00, 0x03,         // MoreData Bit 
//                        0x43, 0x20, 0x20 }  // CDT exchange buffer 
//        );
//
//        ChannelBuffer expectedSetCreditCommand = wrappedBuffer(
//                new byte[] {
//                        0x43, 0x20, 0x20 }  // CDT exchange buffer
//                );
//
//        ChannelBuffer fragment = buffer(1);
//        ChannelBuffer decodedBuffer = null;
//
//        do {
//
//            decodedBuffer = d.poll();
//            assertNull(decodedBuffer);
//
//            fragment.clear();
//            inStream.readBytes(fragment);
//
//            boolean offer = d.offer(fragment);
//
//        } while (inStream.readable());
//
//        d.finish();
//        decodedBuffer = d.poll();
//
//        assertNotNull(decodedBuffer);
//
//        assertEquals(expectedSetCreditCommand, decodedBuffer);

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
