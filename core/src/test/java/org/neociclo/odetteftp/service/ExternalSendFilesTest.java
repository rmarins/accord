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
package org.neociclo.odetteftp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.neociclo.odetteftp.util.OftpTestUtil.getResourceFile;

import java.util.Calendar;

import org.junit.Test;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.support.SessionConfig;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class ExternalSendFilesTest extends AbstractTcpClientExternal {

    private SessionConfig sessionConfig;

    @Override
    protected SessionConfig createSessionConfig() {
        sessionConfig = new SessionConfig();
        sessionConfig.setTransferMode(TransferMode.SENDER_ONLY);
        return sessionConfig;
    }

    @Test
    public void testSendingFile() throws Exception {

        if (!runTests) {
            return;
        }

        VirtualFile fileToSend = createVirtualFile(getResourceFile("data/BR0307108.REM"));

        sendFileTest(null, false, false, 256, 64, fileToSend);
    }

    @Test
    public void testSendingFileWithBufferCompression() throws Exception {

        if (!runTests) {
            return;
        }

        VirtualFile fileToSend = createVirtualFile(getResourceFile("data/AGPLV3"));

        sendFileTest(null, true, false, 4096, 64, fileToSend);
    }

    protected void sendFileTest(OdetteFtpVersion restrictToVersion, boolean compression, boolean restart, int debSize, int cred, VirtualFile fileToSend) throws Exception {

        sessionConfig.setDataExchangeBufferSize(debSize);
        sessionConfig.setVersion(restrictToVersion);
        sessionConfig.setUseCompression(compression);
        sessionConfig.setUseRestart(compression);

        outgoing.offer(fileToSend);

        client.connect(true);

        OdetteFtpObject obj = incoming.poll();
        if (obj instanceof DeliveryNotification) {

            DeliveryNotification notif = (DeliveryNotification) obj;

            assertEquals(fileToSend.getDatasetName(), notif.getDatasetName());

            Calendar c1 = Calendar.getInstance();
            c1.setTime(fileToSend.getDateTime());

            Calendar c2 = Calendar.getInstance();
            c2.setTime(notif.getDateTime());

            assertEquals(c1.get(Calendar.YEAR), c2.get(Calendar.YEAR));
            assertEquals(c1.get(Calendar.MONTH), c2.get(Calendar.MONTH));
            assertEquals(c1.get(Calendar.DATE), c2.get(Calendar.DATE));
            assertEquals(c1.get(Calendar.HOUR), c2.get(Calendar.HOUR));
            assertEquals(c1.get(Calendar.MINUTE), c2.get(Calendar.MINUTE));
            assertEquals(c1.get(Calendar.SECOND), c2.get(Calendar.SECOND));

        } else {
            fail("No delivery notification response. Received an unexpected Odette FTP object: " + obj);
        }

    }
}
