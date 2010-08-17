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
package org.neociclo.odetteftp.protocol;

import static org.neociclo.odetteftp.protocol.CommandExchangeBuffer.*;
import static org.neociclo.odetteftp.util.ProtocolUtil.*;
import static org.neociclo.odetteftp.util.CommandFormatConstants.*;

import static org.junit.Assert.*;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.*;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.startFile;
import static org.neociclo.odetteftp.protocol.v20.ReleaseFormatVer20.*;
import static org.neociclo.odetteftp.protocol.v14.ReleaseFormatVer14.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.neociclo.odetteftp.netty.codec.CommandExchangeBufferBuilder;
import org.neociclo.odetteftp.protocol.v13.CommandBuilderVer13;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;
import org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20;
import org.neociclo.odetteftp.protocol.v20.FileCompression;
import org.neociclo.odetteftp.protocol.v20.FileEnveloping;
import org.neociclo.odetteftp.protocol.v20.SecurityLevel;
import org.neociclo.odetteftp.util.ProtocolUtil;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class CommandExchangeBufferTest {

    private static final String NORMAL_VER14_SSID_TEXT = SSID.getCode()
            + "4O0055LOCALUSER           MYPASSWD02048BYYN064             \r";

    private static final String BEGIN_VER20_EERP_TEXT = EERP.getCode()
            + "MyFileDatasetName         RSV200812081454350123USERDATAO0055DESTINATIONUSER     "
            + "O0055ORIGINATORUSER      ";

    private static final String BEGIN_NO_HASH_AND_SIGNATURE_VER20_EERP_TEXT = EERP.getCode()
            + "MyFileDatasetName            200812081454350123USERDATAO0055DESTINATIONUSER     "
            + "O0055ORIGINATORUSER      ";

    private static final String NORMAL_VER20_ESID_TEXT = ESID.getCode() + "00000\r";

    private static final String BEGIN_VER20_ESID_WITH_DESCRIPTION_TEXT = ESID.getCode() + "00018";

    private static final String NORMAL_TERMINATION_DESC = "NORMAL TERMINATION";

    private static final String NORMAL_VER20_EFID_TEXT = EFID.getCode() + "0000000000000000000000000000017860";

    /**
     * Start File Identification command
     * 
     * dsn: LOOPTEST, date: 20091127, time: 1055440001, orig: O0055ORIGIN,
     * dest: O0055DEST, fmt: U, recl: 0, fsiz: 18, osiz: 256, rest: 7, sec: 03,
     * ciph: 01, comp: 0, env: 1, sign: N, descl: 0.
     */
    private static final String NORMAL_VER20_SFID_TEXT = SFID.getCode() + "LOOPTEST                     20081127"
            + "1055440001        O0055ORIGIN              O0055DEST                U0000000000000000180000000000256"
            + "00000000000000007030101N000";

    /**
     * Certify that Dynamic Fields feature is going to work smoothly on ODETTE
     * FTP v2.0 implementation.
     */
    @Test
    public void testEndResponseBufferWithDynamicFields() throws Exception {

        byte[] eerpCmdBeginning = BEGIN_VER20_EERP_TEXT.getBytes(DEFAULT_PROTOCOL_CHARSET);

        // first: test parsing
        byte[] hash = "ABCDEFGHIJ".getBytes(DEFAULT_PROTOCOL_CHARSET);
        byte[] signature = "ZYXWVUTSRQPOMNLKJIHG".getBytes(DEFAULT_PROTOCOL_CHARSET);

        ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(eerpCmdBeginning, formatBinaryNumber(hash.length, 2), hash,
                formatBinaryNumber(signature.length, 2), signature);

        CommandExchangeBuffer eerp = CommandExchangeBufferBuilder.create(EERP_V20, buffer.duplicate());

        assertEquals("Invalid command identifier.", String.valueOf(EERP.getCode()), eerp.getStringAttribute(EERPCMD_FIELD));
        assertEquals("Invalid dataset name.", "MyFileDatasetName", eerp.getStringAttribute(EERPDSN_FIELD));
        assertEquals("Invalid reserved field.", "RSV", eerp.getStringAttribute(EERPRSV1_FIELD));
        assertEquals("Invalid date.", "20081208", eerp.getStringAttribute(EERPDATE_FIELD));
        assertEquals("Invalid time.", "1454350123", eerp.getStringAttribute(EERPTIME_FIELD));
        assertEquals("Invalid user.", "USERDATA", eerp.getStringAttribute(EERPUSER_FIELD));
        assertEquals("Invalid destination.", "O0055DESTINATIONUSER", eerp.getStringAttribute(EERPDEST_FIELD));
        assertEquals("Invalid originator.", "O0055ORIGINATORUSER", eerp.getStringAttribute(EERPORIG_FIELD));
        assertEquals("Invalid hash length.", hash.length, ProtocolUtil.parseBinaryNumber((byte[]) eerp.getAttribute(EERPHSHL_FIELD)));
        assertTrue("Invalid hash code.", Arrays.equals(hash, (byte[]) eerp.getAttribute(EERPHSH_FIELD)));
        assertEquals("Invalid signature length.", signature.length, ProtocolUtil.parseBinaryNumber((byte[]) eerp.getAttribute(EERPSIGL_FIELD)));
        assertTrue("Invalid signature code.", Arrays.equals(signature, (byte[]) eerp.getAttribute(EERPSIG_FIELD)));

        // then: compare the buffers we got
        byte[] expectedBuf = new byte[buffer.readableBytes()];
        buffer.readBytes(expectedBuf);

        byte[] createdBufferText = eerp.getRawBuffer().array();

        assertTrue("Invalid created buffer text.", Arrays.equals(expectedBuf, createdBufferText));

    }

    /**
     * Certify zero sized dynamic fields works well in ODETTE FTP v2.0
     * implementation.
     */
    @Test
    public void testZeroSizedHashAndSignatureVer20EndResponseBuffer() {

        CommandExchangeBuffer eerp = new CommandExchangeBuffer(EERP_V20);

        eerp.setAttribute(EERPCMD_FIELD, String.valueOf(EERP.getCode()));
        eerp.setAttribute(EERPDSN_FIELD, "MyFileDatasetName");
        eerp.setAttribute(EERPDATE_FIELD, "20081208");
        eerp.setAttribute(EERPTIME_FIELD, "1454350123");
        eerp.setAttribute(EERPUSER_FIELD, "USERDATA");
        eerp.setAttribute(EERPDEST_FIELD, "O0055DESTINATIONUSER");
        eerp.setAttribute(EERPORIG_FIELD, "O0055ORIGINATORUSER");
        eerp.setAttribute(EERPHSHL_FIELD, formatBinaryNumber(0, 2));
        eerp.setAttribute(EERPSIGL_FIELD, formatBinaryNumber(0, 2));

        // compare buffers text
        ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(BEGIN_NO_HASH_AND_SIGNATURE_VER20_EERP_TEXT
                .getBytes(DEFAULT_PROTOCOL_CHARSET), formatBinaryNumber(0, 2), formatBinaryNumber(0, 2));

        assertTrue("Invalid created buffer text.", Arrays.equals(buffer.toByteBuffer().array(), eerp.getRawBuffer().array()));

    }

    /**
     * Check CommandExchangeBuffer still works for previous buffer formats (non
     * ODETTE FTP v2.0 or above).
     */
    @Test
    public void testNormalStartSessionVer14() {

        // first: test parsing

        ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(NORMAL_VER14_SSID_TEXT.getBytes(DEFAULT_PROTOCOL_CHARSET));
        CommandExchangeBuffer ssid = CommandExchangeBufferBuilder.create(SSID_V14, buffer.duplicate());

        assertEquals("Invalid command identifier.", String.valueOf(SSID.getCode()), ssid.getStringAttribute(SSIDCMD_FIELD));
        assertEquals("Invalid protocol level.", 4, Integer.parseInt(ssid.getStringAttribute(SSIDLEV_FIELD)));
        assertEquals("Invalid user code.", "O0055LOCALUSER", ssid.getStringAttribute(SSIDCODE_FIELD));
        assertEquals("Invalid password.", "MYPASSWD", ssid.getStringAttribute(SSIDPSWD_FIELD));
        assertEquals("Invalid exchange buffer size.", 2048, Integer.parseInt(ssid.getStringAttribute(SSIDSDEB_FIELD)));
        assertEquals("Invalid transfer mode.", "B", ssid.getStringAttribute(SSIDSR_FIELD));
        assertEquals("Invalid compression capability.", "Y", ssid.getStringAttribute(SSIDCMPR_FIELD));
        assertEquals("Invalid restart capability.", "Y", ssid.getStringAttribute(SSIDREST_FIELD));
        assertEquals("Invalid window size.", 64, Integer.parseInt(ssid.getStringAttribute(SSIDCRED_FIELD)));
        assertEquals("Non empty reserved field value.", "", ssid.getStringAttribute(SSIDRSV1_FIELD).trim());
        assertEquals("Non empty user data field value.", "", ssid.getStringAttribute(SSIDUSER_FIELD).trim());
        assertEquals("Invalid carriage return.", "\r", ssid.getStringAttribute(SSIDCR_FIELD));

        // then: compare the buffers we got
        String createdBufferText = new String(ssid.getRawBuffer().array());
        assertEquals("Invalid created buffer text.", NORMAL_VER14_SSID_TEXT, createdBufferText);

    }

    @Test
    public void testNormalVer20EndSessionBuffer() throws Exception {

        CommandExchangeBuffer esid = CommandBuilderVer20.endSession(EndSessionReason.NORMAL_TERMINATION, null);

        // compare buffers text
        String createdBufferText = new String(esid.getRawBuffer().array());
        assertEquals("Invalid created buffer text.", NORMAL_VER20_ESID_TEXT, createdBufferText);

    }

    @Test
    public void testNormalVer20EnfFileBuffer() throws Exception {

        CommandExchangeBuffer efid = CommandBuilderVer20.endFile(0, 17860);

        // compare buffers text
        String createdBufferText = new String(efid.getRawBuffer().array());
        assertEquals("Invalid created buffer text.", NORMAL_VER20_EFID_TEXT, createdBufferText);

    }

    @Test
    public void testParseNormalVer20StartFileBuffer() throws Exception {

        ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(NORMAL_VER20_SFID_TEXT.getBytes(DEFAULT_PROTOCOL_CHARSET));
        CommandExchangeBuffer sfid = CommandExchangeBufferBuilder.create(SFID_V20, buffer);

        // compare buffers text
        String createdBufferText = new String(sfid.getRawBuffer().array(), DEFAULT_PROTOCOL_CHARSET);
        assertEquals("Invalid created buffer text.", NORMAL_VER20_SFID_TEXT, createdBufferText);

    }

    @Test
    public void testEndSession20WithDescription() throws Exception {

        // first: test parsing
        byte[] encodedDesc = NORMAL_TERMINATION_DESC.getBytes(CommandExchangeBuffer.UTF8_ENCODED_PROTOCOL_CHARSET);
        byte[] cr = "\r".getBytes(CommandExchangeBuffer.DEFAULT_PROTOCOL_CHARSET);

        ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(BEGIN_VER20_ESID_WITH_DESCRIPTION_TEXT
                .getBytes(DEFAULT_PROTOCOL_CHARSET), encodedDesc, cr);

        CommandExchangeBuffer esid = CommandExchangeBufferBuilder.create(ESID_V20, buffer.duplicate());

        byte[] expected = buffer.toByteBuffer().array();
        byte[] result = esid.getRawBuffer().array();

        assertTrue("Buffers doesn't match.", Arrays.equals(expected, result));
        
    }

    @Test
    public void testStartFileVer13GetRawData() throws Exception {

        String result = "HPAYLOAD                            100630153720        O0055DEST                O0055ORIG                U010240000520000000000";

        Calendar c = Calendar.getInstance();
        c.set(2010, 5, 30, 15, 37, 20);

        CommandExchangeBuffer sfid = CommandBuilderVer13.startFile("PAYLOAD", c.getTime(), null, "O0055DEST",
                "O0055ORIG", RecordFormat.UNSTRUCTURED, 1024, 520, 0);

        byte[] expected = result.getBytes(DEFAULT_PROTOCOL_CHARSET);
        byte[] buffer = sfid.getRawBuffer().array();

        assertTrue(Arrays.equals(expected, buffer));

    }

    @Test
    public void testStartFileVer20GetRawData() throws Exception {

    	Calendar c = Calendar.getInstance();
    	c.set(2010, 7, 17, 10, 16, 34);
    	c.set(Calendar.MILLISECOND, 0);

    	CommandExchangeBuffer sfid = startFile("compressed-7104544892109052014-AGPLV3", c.getTime(), null, "DINET",
    			"DINET", RecordFormat.UNSTRUCTURED, 0, 12, 34,
                0, SecurityLevel.NO_SECURITY_SERVICES, CipherSuite.NO_CIPHER_SUITE_SELECTION, FileCompression.ZLIB,
                FileEnveloping.CMS, false, null);

    	ByteBuffer buffer = sfid.getRawBuffer();

    	assertNotNull(buffer);

    }

}
