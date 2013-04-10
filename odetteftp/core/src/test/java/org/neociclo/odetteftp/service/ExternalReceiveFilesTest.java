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
package org.neociclo.odetteftp.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.neociclo.odetteftp.util.OdetteFtpSupport.getReplyDeliveryNotification;
import static org.neociclo.odetteftp.util.OftpTestUtil.getOutputDir;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.protocol.DefaultEndFileResponse;
import org.neociclo.odetteftp.protocol.DefaultStartFileResponse;
import org.neociclo.odetteftp.oftplet.EndFileResponse;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.support.OftpletEventListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class ExternalReceiveFilesTest extends AbstractTcpClientExternal {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalReceiveFilesTest.class);

    private OdetteFtpConfiguration sessionConfig;

    @Override
    protected OdetteFtpConfiguration createSessionConfig() {
        sessionConfig = new OdetteFtpConfiguration();
        sessionConfig.setTransferMode(TransferMode.RECEIVER_ONLY);
        return sessionConfig;
    }

    @Test
    public void testReceiveFiles() throws Exception {

        if (!runTests) {
            return;
        }

        factory.setEventListener(new OftpletEventListenerAdapter() {

            @Override
            public StartFileResponse acceptStartFile(VirtualFile virtualFile) {

                File saveToFile = createIncomingFile(virtualFile);

                DefaultStartFileResponse response = DefaultStartFileResponse.positiveStartFileAnswer(saveToFile);

                return response;
            }

            @Override
            public void onReceiveFileStart(VirtualFile virtualFile, long answerCount) {
                LOGGER.debug("Begin receiving file: {}", virtualFile);
            }

            @Override
            public EndFileResponse onReceiveFileEnd(VirtualFile virtualFile, long recordCount, long unitCount) {
                LOGGER.debug("Receive file completed: {}", virtualFile);

                // reply with EERP (positive delivery notification)
                DeliveryNotification notif = getReplyDeliveryNotification(virtualFile);
                outgoing.offer(notif);
                return DefaultEndFileResponse.positiveEndFileAnswer();
            }
        });

        connect(true);

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
