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
