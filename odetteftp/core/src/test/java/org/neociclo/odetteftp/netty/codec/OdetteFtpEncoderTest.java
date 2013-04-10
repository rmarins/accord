/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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
import static org.neociclo.odetteftp.protocol.CommandExchangeBuffer.DEFAULT_PROTOCOL_CHARSET;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.junit.Before;
import org.junit.Test;
import org.neociclo.odetteftp.EntityType;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.protocol.CommandBuilder;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OdetteFtpEncoderTest {

    private OftpSessionSetterHandler sessionHandler;
    private EncoderEmbedder<ChannelBuffer> e;

    @Before
    public void setUp() {
        sessionHandler = new OftpSessionSetterHandler(new OdetteFtpEncoder());
        e = new EncoderEmbedder<ChannelBuffer>(sessionHandler);
    }

    @Test
    public void testEncodeSetCreditCommand() throws Exception {

        OdetteFtpSession s = new OdetteFtpSession(EntityType.INITIATOR);
        sessionHandler.setSession(s);

        CommandExchangeBuffer cdt = CommandBuilder.setCredit();

        ChannelBuffer cdtBuffer = ChannelBuffers.wrappedBuffer("C  ".getBytes(DEFAULT_PROTOCOL_CHARSET));

        for (OdetteFtpVersion ver : OdetteFtpVersion.values()) {

            s.setVersion(ver);

            e.offer(cdt);
            ChannelBuffer encodedBuffer = e.poll();

            assertNotNull(encodedBuffer);
            assertEquals(3, encodedBuffer.capacity());
            assertEquals(cdtBuffer, encodedBuffer);

        }

        e.finish();

    }

}
