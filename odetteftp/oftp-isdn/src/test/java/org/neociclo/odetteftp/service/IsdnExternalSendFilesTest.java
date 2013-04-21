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
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;

/**
 * @author Rafael Marins
 * @version $Rev$
 */
public class IsdnExternalSendFilesTest extends AbstractIsdnClientExternal {

    private OdetteFtpConfiguration sessionConfig;

    @Override
    protected OdetteFtpConfiguration createSessionConfig() {
        sessionConfig = new OdetteFtpConfiguration();
        sessionConfig.setTransferMode(TransferMode.SENDER_ONLY);
        return sessionConfig;
    }

    @Test
    public void testSendingFile() throws Exception {

        if (!runTests) {
            return;
        }

        VirtualFile fileToSend = createVirtualFile("MY_%d", null, 0, 0, getResourceFile("data/TEST"));

        sendFileTest(null, false, false, 256, 64, fileToSend);
    }

//    @Test
//    public void testSendingFileWithBufferCompression() throws Exception {
//
//        if (!runTests) {
//            return;
//        }
//
//        VirtualFile fileToSend = createVirtualFile(getResourceFile("data/AGPLV3"));
//
//        sendFileTest(null, true, false, 4096, 64, fileToSend);
//    }

    protected void sendFileTest(OdetteFtpVersion restrictToVersion, boolean compression, boolean restart, int debSize, int cred, VirtualFile fileToSend) throws Exception {

        sessionConfig.setDataExchangeBufferSize(debSize);
        sessionConfig.setVersion(restrictToVersion);
        sessionConfig.setUseCompression(compression);
        sessionConfig.setUseRestart(compression);

        outgoing.offer(fileToSend);

        connect();

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
