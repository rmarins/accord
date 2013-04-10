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
package org.neociclo.filetransfer.oftp;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.support.TransportType;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OdetteFtpClientConfigurationTest {

    private static final String FULL_OFTP_CONNECTION_STRING = "oftp://" +
    		"O0055THEUSEROID@localhost:13305/?" +
    		"version=OFTP_V20&" +
    		"mode=SENDER_ONLY&" +
    		"transport=TCPIP&" +
    		"customLongDsn=false&" +
    		"timeout=90&" +
    		"debSize=4096&" +
    		"windowSize=64&" +
    		"restart=true&" +
    		"compression=true&" +
    		"secureAuth=true&" +
    		"secureAuthKeystore=myCertFile.p12&" +
    		"secureAuthKeystorePassword=neociclo";

    @Test
    public void testFullOftpConnectionString() throws Exception {
        OdetteFtpClientConfiguration cfg = new OdetteFtpClientConfiguration(FULL_OFTP_CONNECTION_STRING);

        assertFalse("SSL must be disabled for 'oftp' scheme.", cfg.isSsl());
        assertEquals("localhost", cfg.getHost());
        assertEquals(13305, (int) cfg.getPort());
        assertEquals("O0055THEUSEROID", cfg.getUserOid());

        assertEquals(OdetteFtpVersion.OFTP_V20, cfg.getVersion());
        assertEquals(TransferMode.SENDER_ONLY, cfg.getMode());
        assertEquals(TransportType.TCPIP, cfg.getTransport());

    }
}
