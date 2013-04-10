/**
 * The Accord Project, http://accordproject.org
 * Copyright (C) 2005-2013 Rafael Marins, http://rafaelmarins.com
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
 */
package org.neociclo.odetteftp.netty.codec;

import static org.junit.Assert.*;
import static org.neociclo.odetteftp.util.CommandFormatConstants.*;
import static org.neociclo.odetteftp.protocol.CommandExchangeBuffer.*;

import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.Before;
import org.junit.Test;
import org.neociclo.odetteftp.EntityType;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.netty.codec.OdetteFtpDecoder;
import org.neociclo.odetteftp.protocol.CommandBuilder;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.CommandIdentifier;
import org.neociclo.odetteftp.protocol.EndSessionReason;

/**
 * @author Rafael Marins
 */
public class OdetteFtpDecoderTest {

    private static ChannelBuffer command(String text) throws UnsupportedEncodingException {
        return ChannelBuffers.wrappedBuffer(text.getBytes(DEFAULT_PROTOCOL_CHARSET));
    }

    private OftpSessionSetterHandler sessionHandler;
    private DecoderEmbedder<ChannelBuffer> d;

    @Before
    public void setUp() {
        sessionHandler = new OftpSessionSetterHandler(new OdetteFtpDecoder());
        d = new DecoderEmbedder<ChannelBuffer>(sessionHandler);
    }

    @Test
    public void testDecodeSetCreditCommand() throws Exception {

        OdetteFtpSession s = new OdetteFtpSession(EntityType.INITIATOR);
        sessionHandler.setSession(s);

        for (OdetteFtpVersion ver : OdetteFtpVersion.values()) {

            s.setVersion(ver);

            d.offer(command("C  "));
            CommandExchangeBuffer cdt = (CommandExchangeBuffer) d.poll();

            assertEquals(CommandIdentifier.CDT, cdt.getIdentifier());
            assertEquals("", cdt.getStringAttribute(CDTRSV1_FIELD));

            assertEquals(CommandBuilder.setCredit(), cdt);
        }

        d.finish();

    }

    /**
     * Simulate the receive of an ESID in older command format when the
     * Initiator began the communication on OFTP version 2 and expects the
     * extended command format of the lastest protocol.
     */
    @Test
    public void testDecodeVer13EndSessionCommandOnVer20Session() throws Exception {

        OdetteFtpSession s = new OdetteFtpSession(EntityType.INITIATOR);
        s.setVersion(OdetteFtpVersion.OFTP_V20);
        sessionHandler.setSession(s);

        d.offer(command("F03\r"));
        d.finish();

        CommandExchangeBuffer esid = (CommandExchangeBuffer) d.poll();

        assertEquals(CommandIdentifier.ESID, esid.getIdentifier());
        assertEquals(EndSessionReason.UNKNOWN_USER_CODE.getCode(), Integer.parseInt(esid.getStringAttribute(ESIDREAS_FIELD)));

    }

}
