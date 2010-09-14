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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.neociclo.odetteftp.util.OdetteFtpSupport.getReplyDeliveryNotification;
import static org.neociclo.odetteftp.util.OftpTestUtil.getOutputDir;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DefaultStartFileResponse;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.support.InOutOftpletEventListenerAdapter;
import org.neociclo.odetteftp.support.SessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class ExternalReceiveFilesTest extends AbstractTcpClientExternal {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalReceiveFilesTest.class);

    private SessionConfig sessionConfig;

    @Override
    protected SessionConfig createSessionConfig() {
        sessionConfig = new SessionConfig();
        sessionConfig.setTransferMode(TransferMode.RECEIVER_ONLY);
        return sessionConfig;
    }

    @Test
    public void testReceiveFiles() throws Exception {

        if (!runTests) {
            return;
        }

        factory.setEventListener(new InOutOftpletEventListenerAdapter() {

            @Override
            public StartFileResponse acceptStartFile(VirtualFile virtualFile) {

                File saveToFile = createIncomingFile(virtualFile);

                DefaultStartFileResponse response = new DefaultStartFileResponse(true);
                response.setFile(saveToFile);

                return response;
            }

            @Override
            public void onReceiveFileStart(VirtualFile virtualFile, long answerCount) {
                LOGGER.debug("Begin receiving file: {}", virtualFile);
            }

            @Override
            public boolean onReceiveFileEnd(VirtualFile virtualFile, long recordCount, long unitCount) {
                LOGGER.debug("Receive file completed: {}", virtualFile);

                // reply with EERP (positive delivery notification)
                DeliveryNotification notif = getReplyDeliveryNotification(virtualFile);
                outgoing.offer(notif);
                return true;
            }
        });

        client.connect(true);

        VirtualFile vf = (VirtualFile) incoming.poll();

        do {
            assertNotNull(vf);

            assertNotNull(vf.getFile());
            assertTrue(vf.getFile().exists());

            LOGGER.trace("Deleting received file: {}", vf.getFile());
            vf.getFile().delete();
        } while (!incoming.isEmpty());

    }

    protected File createIncomingFile(VirtualFile incomingFile) {

        File dir = getOutputDir();
        File saveTo = null;

        try {
            saveTo = File.createTempFile("in-", "-" + incomingFile.getDatasetName(), dir);
        } catch (IOException e) {
            // just do logging
            LOGGER.error("Cannot create temp file. Output Dir=" + dir, e);
        }

        return saveTo;
    }

}
