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
