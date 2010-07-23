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
import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V20;
import static org.neociclo.odetteftp.util.OftpTestUtil.getResourceFile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.client.events.DeliveryNotificationEvent;
import org.neociclo.odetteftp.client.events.IncomingEventListener;
import org.neociclo.odetteftp.client.events.OdetteFtpEvent;
import org.neociclo.odetteftp.protocol.DeliveryNotificationInfo;
import org.neociclo.odetteftp.protocol.VirtualFileInfo;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;
import org.neociclo.odetteftp.protocol.v20.DefaultEnvelopedVirtualFile;
import org.neociclo.odetteftp.protocol.v20.FileCompression;
import org.neociclo.odetteftp.protocol.v20.SecurityLevel;
import org.neociclo.odetteftp.test.ExternalConnectionTemplate;
import org.neociclo.odetteftp.util.OftpTestUtil;
import org.neociclo.odetteftp.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class LocalOftp20ExternalConnectionTest extends ExternalConnectionTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalOftp20ExternalConnectionTest.class);

    private static final boolean USE_SSL = true;

    private static final boolean USE_SECURE_AUTHENTICATION = true;

    private static final String CERT_PARENT_PATH = "certificates";

    private static final String CERT_EXT = ".cer";

    private static final String KEYSTORE_PATH = "keystores/client-bogus.p12";

    private static final String KEYSTORE_PSWD = "neociclo";

    private KeyStore userKeyStore;

    @Override
    protected OdetteFtpVersion getTestResponderVersion() {
        return OFTP_V20;
    }

    @Override
    protected X509Certificate lookupUserCertificate() {

        X509Certificate cert = null;

        try {
            openUserKeystore();
            cert = SecurityUtil.getCertificateEntry(userKeyStore);
        } catch (Exception e) {
            LOGGER.error("Lookup User Certificate failed.", e);
        }

        return cert;
    }

    @Override
    protected PrivateKey lookupUserPrivateKey() {

        PrivateKey key = null;

        try {
            openUserKeystore();
            key = SecurityUtil.getPrivateKey(userKeyStore, KEYSTORE_PSWD.toCharArray());
        } catch (Exception e) {
            LOGGER.error("Lookup User PrivateKey failed", e);
        }

        return key;
    }

    @Override
    protected X509Certificate lookupPartnerCertificate(String oid) {

        X509Certificate cert = null;

        if (oid == null || "DINET".equals(oid)) {
            oid = "o094200005562851534tarnfo";
        }

        String certPath = CERT_PARENT_PATH + File.separatorChar + oid.toLowerCase() + CERT_EXT;

        try {
            File certFile = OftpTestUtil.getResourceFile(certPath);
            cert = SecurityUtil.openCertificate(certFile);
        } catch (Exception e) {
            LOGGER.error("Lookup Partner Public Certificate failed", e);
        }

        return cert;
    }

    @Override
    protected ClientParameters createClientParameters(String oid) {
        ClientParameters cfg = super.createClientParameters(oid);
        cfg.setSsl(USE_SSL);
        cfg.setSecureAuthentication(USE_SECURE_AUTHENTICATION);

        // try {
        // ksfile = OftpTestUtil.getResourceFile(KEYSTORE_PATH);
        // cfg.setKeystoreFilePath(ksfile.getAbsolutePath());
        // cfg.setKeystorePassword(KEYSTORE_PSWD);
        // } catch (URISyntaxException e) {
        // LOG.error("Unable to use Keystore: " + KEYSTORE_PATH);
        // }
        return cfg;
    }

    @Test
    public void testConnectAndDisconnect() throws Exception {

        if (!testExternal) return;

        runTransfer();

    }

    @Test
    public void testTransmitCompressed() throws Exception {

        if (!testExternal) return;

        /* accept only incoming delivery notification */
        client.addRetrieveFilter(new IRetrieveFilter() {
            public boolean accept(VirtualFileInfo exchangeInfo) { return false; }
            public boolean accept(DeliveryNotificationInfo notif) { return true; }
        });

        /* use holder class to keep the received acknowledgment */
        final TransferHolder holder = new TransferHolder();
        client.addListener(new IncomingEventListener() {
            public void handleOdetteFtpEvent(OdetteFtpEvent event) {
                if (event instanceof DeliveryNotificationEvent) {
                    DeliveryNotificationEvent notif = (DeliveryNotificationEvent) event;
                    LOGGER.debug("Acknowledgment received: " + notif.getNotification());
                    holder.setNotif(notif.getNotification());
                }
            }
        });

        /* schedule to send a local file */
        Date payloadDate = Calendar.getInstance().getTime();
        String payloadName = "LOOPTEST";

        DefaultEnvelopedVirtualFile envelopedInfo = new DefaultEnvelopedVirtualFile();
        envelopedInfo.setDatasetName(payloadName);
        envelopedInfo.setDateTime(payloadDate);
//        envelopedInfo.setDestination("O094200005562851534TARNFO");
        envelopedInfo.setCompressionAlgorithm(FileCompression.ZLIB);

        File payload = getResourceFile("data/BR0307108.REM");
        OftpFile localFile = new OftpFile(payload, envelopedInfo);

        client.addSendFile(localFile);

        runTransfer();

        assertNotNull(holder);

        assertEquals(payloadName, holder.getNotif().getDatasetName());
        assertEquals(payloadDate, holder.getNotif().getDateTime());

    }

    @Test
    public void testTransmitEncrypted() throws Exception {

        if (!testExternal) return;

        /* accept only incoming delivery notification */
        client.addRetrieveFilter(new IRetrieveFilter() {
            public boolean accept(VirtualFileInfo exchangeInfo) { return false; }
            public boolean accept(DeliveryNotificationInfo notif) { return true; }
        });

        /* use holder class to keep the received acknowledgment */
        final TransferHolder holder = new TransferHolder();
        client.addListener(new IncomingEventListener() {
            public void handleOdetteFtpEvent(OdetteFtpEvent event) {
                if (event instanceof DeliveryNotificationEvent) {
                    DeliveryNotificationEvent notif = (DeliveryNotificationEvent) event;
                    LOGGER.debug("Acknowledgment received: " + notif.getNotification());
                    holder.setNotif(notif.getNotification());
                }
            }
        });

        /* schedule to send a local file */
        Date payloadDate = Calendar.getInstance().getTime();
        String payloadName = "LOOPTEST_ENCRYPTED";

        DefaultEnvelopedVirtualFile envelopedInfo = new DefaultEnvelopedVirtualFile();
        envelopedInfo.setDatasetName(payloadName);
        envelopedInfo.setDateTime(payloadDate);
//        envelopedInfo.setDestination("O094200005562851534TARNFO");
        envelopedInfo.setSecurityLevel(SecurityLevel.ENCRYPTED);
        envelopedInfo.setCipherSuite(CipherSuite.TRIPLEDES_RSA_SHA1);

        File payload = getResourceFile("data/BR0307108.REM");
        OftpFile localFile = new OftpFile(payload, envelopedInfo);

        client.addSendFile(localFile);

        runTransfer();

        assertNotNull(holder);

        assertEquals(payloadName, holder.getNotif().getDatasetName());
        assertEquals(payloadDate, holder.getNotif().getDateTime());

    }

    @Test
    public void testTransmitCompressedEncrypted() throws Exception {

        if (!testExternal) return;

        /* accept only incoming delivery notification */
        client.addRetrieveFilter(new IRetrieveFilter() {
            public boolean accept(VirtualFileInfo exchangeInfo) { return false; }
            public boolean accept(DeliveryNotificationInfo notif) { return true; }
        });

        /* use holder class to keep the received acknowledgment */
        final TransferHolder holder = new TransferHolder();
        client.addListener(new IncomingEventListener() {
            public void handleOdetteFtpEvent(OdetteFtpEvent event) {
                if (event instanceof DeliveryNotificationEvent) {
                    DeliveryNotificationEvent notif = (DeliveryNotificationEvent) event;
                    LOGGER.debug("Acknowledgment received: " + notif.getNotification());
                    holder.setNotif(notif.getNotification());
                }
            }
        });

        /* schedule to send a local file */
        Date payloadDate = Calendar.getInstance().getTime();
        String payloadName = "LOOPTEST_ZLIB_3DES";

        DefaultEnvelopedVirtualFile envelopedInfo = new DefaultEnvelopedVirtualFile();
        envelopedInfo.setDatasetName(payloadName);
        envelopedInfo.setDateTime(payloadDate);
//        envelopedInfo.setDestination("O094200005562851534TARNFO");
        envelopedInfo.setCompressionAlgorithm(FileCompression.ZLIB);
        envelopedInfo.setSecurityLevel(SecurityLevel.ENCRYPTED);
        envelopedInfo.setCipherSuite(CipherSuite.TRIPLEDES_RSA_SHA1);

        File payload = getResourceFile("data/BR0307108.REM");
        OftpFile localFile = new OftpFile(payload, envelopedInfo);

        client.addSendFile(localFile);

        runTransfer();

        assertNotNull(holder);

        assertEquals(payloadName, holder.getNotif().getDatasetName());
        assertEquals(payloadDate, holder.getNotif().getDateTime());

    }

    @Test
    public void testTransmitFullCms() throws Exception {

        if (!testExternal) return;

        /* accept only incoming delivery notification */
        client.addRetrieveFilter(new IRetrieveFilter() {
            public boolean accept(VirtualFileInfo exchangeInfo) { return false; }
            public boolean accept(DeliveryNotificationInfo notif) { return true; }
        });

        /* use holder class to keep the received acknowledgment */
        final TransferHolder holder = new TransferHolder();
        client.addListener(new IncomingEventListener() {
            public void handleOdetteFtpEvent(OdetteFtpEvent event) {
                if (event instanceof DeliveryNotificationEvent) {
                    DeliveryNotificationEvent notif = (DeliveryNotificationEvent) event;
                    LOGGER.debug("Acknowledgment received: " + notif.getNotification());
                    holder.setNotif(notif.getNotification());
                }
            }
        });

        /* schedule to send a local file */
        Date payloadDate = Calendar.getInstance().getTime();
        String payloadName = "LOOPTEST_FULL";

        DefaultEnvelopedVirtualFile envelopedInfo = new DefaultEnvelopedVirtualFile();
        envelopedInfo.setDatasetName(payloadName);
        envelopedInfo.setDateTime(payloadDate);
//        envelopedInfo.setDestination("O094200005562851534TARNFO");
        envelopedInfo.setCompressionAlgorithm(FileCompression.ZLIB);
        envelopedInfo.setSecurityLevel(SecurityLevel.ENCRYPTED_AND_SIGNED);
        envelopedInfo.setCipherSuite(CipherSuite.TRIPLEDES_RSA_SHA1);

        File payload = getResourceFile("data/BR0307108.REM");
        OftpFile localFile = new OftpFile(payload, envelopedInfo);

        client.addSendFile(localFile);

        runTransfer();

        assertNotNull(holder);

        assertEquals(payloadName, holder.getNotif().getDatasetName());
        assertEquals(payloadDate, holder.getNotif().getDateTime());

    }

    @Test
    public void testReceiveCompressed() throws Exception {

        if (!testExternal) return;

        final String payloadName = "LOOPTEST";

        /* accept only incoming delivery notification */
        client.addRetrieveFilter(new IRetrieveFilter() {
            public boolean accept(VirtualFileInfo exchangeInfo) {
                LOGGER.debug("Accept: {}", exchangeInfo);
                return (payloadName.equalsIgnoreCase(exchangeInfo.getDatasetName()));
            }

            public boolean accept(DeliveryNotificationInfo notif) { return false; }
        });

        runTransfer();

        OftpFile retrievedFile = client.inFilesQueue.poll();
        
        assertNotNull(retrievedFile);
        assertNotNull(retrievedFile.getUserFile());

    }

    @Test
    public void testReceiveAny() throws Exception {

        if (!testExternal) return;

        /* accept only incoming delivery notification */
        client.addRetrieveFilter(new RetrieveAllFilter());

        runTransfer();

        OftpFile retrievedFile = client.inFilesQueue.poll();
        
        assertNotNull(retrievedFile);
        assertNotNull(retrievedFile.getUserFile());

    }

    private void openUserKeystore() throws URISyntaxException, KeyStoreException, NoSuchProviderException,
            NoSuchAlgorithmException, CertificateException, IOException {
        if (userKeyStore == null) {
            File ksfile = OftpTestUtil.getResourceFile(KEYSTORE_PATH);
            userKeyStore = SecurityUtil.openKeyStore(ksfile, KEYSTORE_PSWD.toCharArray());
        }
    }

    private static class TransferHolder {

        private DeliveryNotificationInfo notif;

        private OftpFile localFile;

        public OftpFile getLocalFile() {
            return localFile;
        }

        public void setLocalFile(OftpFile localFile) {
            this.localFile = localFile;
        }

        public DeliveryNotificationInfo getNotif() {
            return notif;
        }

        public void setNotif(DeliveryNotificationInfo notif) {
            this.notif = notif;
        }
    }

}
