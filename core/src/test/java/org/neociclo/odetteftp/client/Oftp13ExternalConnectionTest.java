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
import static org.junit.Assert.fail;
import static org.neociclo.odetteftp.OdetteFtpVersion.*;
import static org.neociclo.odetteftp.util.OftpTestUtil.getResourceFile;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.client.events.DeliveryNotificationEvent;
import org.neociclo.odetteftp.client.events.FileTransferEndEvent;
import org.neociclo.odetteftp.client.events.IncomingEventListener;
import org.neociclo.odetteftp.client.events.OdetteFtpEvent;
import org.neociclo.odetteftp.protocol.DeliveryNotificationInfo;
import org.neociclo.odetteftp.protocol.VirtualFileInfo;
import org.neociclo.odetteftp.test.ExternalConnectionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class Oftp13ExternalConnectionTest extends ExternalConnectionTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(Oftp13ExternalConnectionTest.class);

    @Override
    protected OdetteFtpVersion getTestResponderVersion() { return OFTP_V13; }
    
    @Test
    public void testConnectAndDisconnect() throws Exception {

        if (!testExternal) return;

        try {
            client.transfer();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown on transfer() method.");
            throw e;
        }

    }

    @Test
    public void testReceiveAll() throws Exception {

        if (!testExternal) return;

        client.addRetrieveFilter(new RetrieveAllFilter());

        client.addListener(new IncomingEventListener() {
            public void handleOdetteFtpEvent(OdetteFtpEvent event) {
                if (event instanceof FileTransferEndEvent) {
                    FileTransferEndEvent endReceive = (FileTransferEndEvent) event;
                    File inFile = endReceive.getVirtualFile().getFile();
                    LOG.debug("File received: " + inFile);
                }
            }
        });

        try {
            client.transfer();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown on transfer() method.");
            throw e;
        }

    }

    @Test
    public void testTransmitAndExpectAcnkowledge() throws Exception {

        if (!testExternal) return;

        /* accept only incoming delivery notification */
        client.addRetrieveFilter(new IRetrieveFilter() {
            public boolean accept(VirtualFileInfo exchangeInfo) { return false; }
            public boolean accept(DeliveryNotificationInfo notif) { return true; }
        });

        /* use holder class to keep the received acknowledgment */
        final NotifHolder holder = new NotifHolder();
        client.addListener(new IncomingEventListener() {
            public void handleOdetteFtpEvent(OdetteFtpEvent event) {
                if (event instanceof DeliveryNotificationEvent) {
                    DeliveryNotificationEvent notif = (DeliveryNotificationEvent) event;
                    LOG.debug("Acknowledgment received: " + notif.getNotification());
                    holder.setNotif(notif.getNotification());
                }
            }
        });

        /* schedule to send a local file */
        Date payloadDate = Calendar.getInstance().getTime();
        String payloadName = "ORDER";

        VirtualFileInfo info = new VirtualFileInfo();
        info.setDatasetName(payloadName);
        info.setDateTime(payloadDate);
//        info.setDestination("O0055NEOCICLO");

        File payload = getResourceFile("data/BR0307108.REM");
        OftpFile localFile = new OftpFile(payload, info);

        client.addSendFile(localFile);

        try {
            client.transfer();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown on transfer() method.");
            throw e;
        }

        assertNotNull(holder);
        assertNotNull(holder.getNotif());

        assertEquals(payloadName, holder.getNotif().getDatasetName());
        assertEquals(payloadDate.getTime(), holder.getNotif().getDateTime().getTime());

    }

    private static class NotifHolder {

        private DeliveryNotificationInfo notif;

        public DeliveryNotificationInfo getNotif() {
            return notif;
        }

        public void setNotif(DeliveryNotificationInfo notif) {
            this.notif = notif;
        }
    }
}
