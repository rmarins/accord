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
package org.neociclo.odetteftp.client;

import static org.neociclo.odetteftp.protocol.v20.FileCompression.ZLIB;
import static org.neociclo.odetteftp.util.OftpTestUtil.getResourceFile;

import java.io.File;

import org.junit.Test;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.client.events.FileTransferEndEvent;
import org.neociclo.odetteftp.client.events.OdetteFtpEvent;
import org.neociclo.odetteftp.client.events.OutgoingEventListener;
import org.neociclo.odetteftp.protocol.VirtualFileInfo;
import org.neociclo.odetteftp.protocol.v20.DefaultEnvelopedVirtualFile;
import org.neociclo.odetteftp.test.SecureEmbeddedTestTemplate;
import org.neociclo.odetteftp.test.TransferHolder;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpVer20ClientTest extends SecureEmbeddedTestTemplate {

    private static final String TEST_FILE_PATH = "data/BR0307108.REM";

    @Override
    protected OdetteFtpVersion getTestResponderVersion() {
        return OdetteFtpVersion.OFTP_V20;
    }

    @Override
    public void setUp() throws Exception {
        client = createClient("O0055partnera", "neociclo");
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        client = null;
        super.tearDown();
    }

    @Override
    protected ClientParameters createClientParameters(String oid) {
        // TODO add embedded server support secureAuthentication session param
        ClientParameters connectConfig = super.createClientParameters(oid);
        connectConfig.setSecureAuthentication(false);
        return connectConfig;
    }

    @Test
    public void testSecureConnection() throws Exception {

        /*
         * call transfer with no additional client configuration to perform
         * connection & disconnection
         */

        runTransfer();

    }

    @Test
    public void testSendingCompressedData() throws Exception {

        File payload = getResourceFile(TEST_FILE_PATH);

        /* use holder class to keep the event and assert later */
        final TransferHolder holder = new TransferHolder();
        client.addListener(new OutgoingEventListener() {
            public void handleOdetteFtpEvent(OdetteFtpEvent event) {
                if (event instanceof FileTransferEndEvent) {
                    FileTransferEndEvent endEvent = (FileTransferEndEvent) event;
                    holder.setTransferInfo(endEvent.getMappingInfo());
                }
            }
        });

        /* schedule to send the payload */
        DefaultEnvelopedVirtualFile mapInfo = createMappingInfo(payload);
        mapInfo.setCompressionAlgorithm(ZLIB);
        OftpFile localFile = new OftpFile(payload, mapInfo);

        client.addSendFile(localFile);

        /* do client transfers and check if file sent is confirmed */
        runTransfer();

    }

    @Override
    protected DefaultEnvelopedVirtualFile createMappingInfo(File payload) {
        VirtualFileInfo base = super.createMappingInfo(payload);
        DefaultEnvelopedVirtualFile enveloped = new DefaultEnvelopedVirtualFile(base);
        return enveloped;
    }

}
