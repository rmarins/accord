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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.neociclo.odetteftp.util.OftpTestUtil.getResourceFile;

import java.io.File;

import org.junit.Test;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.client.events.FileTransferEndEvent;
import org.neociclo.odetteftp.client.events.OdetteFtpEvent;
import org.neociclo.odetteftp.client.events.OutgoingEventListener;
import org.neociclo.odetteftp.protocol.VirtualFileInfo;
import org.neociclo.odetteftp.test.EmbeddedTestTemplate;
import org.neociclo.odetteftp.test.TransferHolder;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OdetteFtpClientTest extends EmbeddedTestTemplate {

    private static final String TEST_FILE_PATH = "data/BR0307108.REM";

    @Override
    protected OdetteFtpVersion getTestResponderVersion() {
        return OdetteFtpVersion.OFTP_V14;
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

    @Test
    public void testSendOutgoingFileTransferRequest() throws Exception {

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
        VirtualFileInfo mapInfo = createMappingInfo(payload);
        OftpFile localFile = new OftpFile(payload, mapInfo);

        client.addSendFile(localFile);

        /* do client transfers and check if file sent is confirmed */
        runTransfer();

        assertNotNull("Any event for the payload transmit complete.", holder.getTransferInfo());
        assertEquals("Incorrect transmitted file DataSetName.", mapInfo.getDatasetName(), holder.getTransferInfo()
                .getDatasetName());
        assertEquals("Incorrect transmitted file Date & Time.", mapInfo.getDateTime(), holder.getTransferInfo()
                .getDateTime());

    }
}
