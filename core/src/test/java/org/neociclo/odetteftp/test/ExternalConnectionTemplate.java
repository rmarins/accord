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
package org.neociclo.odetteftp.test;


import static org.junit.Assert.fail;
import static org.neociclo.odetteftp.support.TransportType.TCPIP;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.junit.After;
import org.junit.Before;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.client.ClientParameters;
import org.neociclo.odetteftp.client.OdetteFtpClient;
import org.neociclo.odetteftp.client.transfer.TransientClient;
import org.neociclo.odetteftp.security.ISecurityContext;
import org.neociclo.odetteftp.security.PartnerCertificateCallback;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.security.UserCertificateCallback;
import org.neociclo.odetteftp.security.UserPrivateKeyCallback;
import org.neociclo.odetteftp.util.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public abstract class ExternalConnectionTemplate {

    public static final int OFTP_TEST_PORT = 3305;

    public static final String OFTP_TEST_HOST = "localhost";

    public static final String OFTP_TEST_OID = "o0055partnera";

    public static final String OFTP_TEST_PWD = "neociclo";

    public static final String OFTP_TEST_EXTERNAL = "accord.oftp.external";

    public static final String OFTP_TEST_PORT_PROPERTY = "accord.oftp.test.port";

    public static final String OFTP_TEST_HOST_PROPERTY = "accord.oftp.test.host";

    public static final String OFTP_TEST_OID_PROPERTY = "accord.oftp.test.oid";

    public static final String OFTP_TEST_PWD_PROPERTY = "accord.oftp.test.password";

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalConnectionTemplate.class);

    private static final File TEST_CLIENT_TMP_DIR = new File("test-client-tmp");

    protected static Thread cleanTempDirsThread;

    protected OdetteFtpClient client;

    protected boolean testExternal;

    public ExternalConnectionTemplate() {
        super();

        if (cleanTempDirsThread == null) {
            LOGGER.debug("ExternalConnectionTemplate activated shutdownHook callback to clean up temp directories.");
            cleanTempDirsThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        cleanTmpDirs();
                    } catch (IOException e) {
                        LOGGER.error("Temp directories clean up failure.", e);
                    }
                }
            });
            Runtime.getRuntime().addShutdownHook(cleanTempDirsThread);
        }
    }

    protected abstract OdetteFtpVersion getTestResponderVersion();

    @Before
    public void setUp() throws Exception {
        testExternal = Boolean.parseBoolean(System.getProperty(OFTP_TEST_EXTERNAL, "false"));
        if (!testExternal)
            return;
        initDirs();
        client = createClient();
    }

    @After
    public void tearDown() throws Exception {
        if (!testExternal)
            return;
        client = null;
    }

    protected ClientParameters createClientParameters(String oid) {

        ClientParameters connectConfig = new ClientParameters();

        String host = System.getProperty(OFTP_TEST_HOST_PROPERTY, getConnectionHost());
        int port = Integer.parseInt(System.getProperty(OFTP_TEST_PORT_PROPERTY, String.valueOf(getConnectionPort())));

        connectConfig.setHost(host);
        connectConfig.setPort(port);
        connectConfig.setOid(oid);
        connectConfig.setVersion(getTestResponderVersion());

        connectConfig.setTransport(TCPIP);

        connectConfig.setWindowSize(2);
        connectConfig.setDebSize(1024);

        connectConfig.setSsl(false);
        connectConfig.setLoggingEnabled(true);
        connectConfig.setLoggingLevel("DEBUG");

        return connectConfig;
    }

    protected X509Certificate lookupUserCertificate() { return null; }

    protected X509Certificate lookupPartnerCertificate(String oid) { return null; }

    protected PrivateKey lookupUserPrivateKey() { return null; }

    protected ISecurityContext createSecurityContext(final String oid, final String pwd) {

        final CallbackHandler callbackHandler = new CallbackHandler() {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback cb : callbacks) {
                    if (cb instanceof PasswordCallback) {
                        PasswordCallback pwdcb = (PasswordCallback) cb;
                        if (oid.equalsIgnoreCase(pwdcb.getPrompt())) {
                            pwdcb.setPassword(pwd);
                        }
                    } else if (cb instanceof PartnerCertificateCallback) {
                        PartnerCertificateCallback partcb = (PartnerCertificateCallback) cb;
                        X509Certificate cert = lookupPartnerCertificate(partcb.getUserCode());
                        partcb.setCertificate(cert);
                    } else if (cb instanceof UserCertificateCallback) {
                        UserCertificateCallback usercb = (UserCertificateCallback) cb;
                        X509Certificate cert = lookupUserCertificate();
                        usercb.setCertificate(cert);
                    } else if (cb instanceof UserPrivateKeyCallback) {
                        UserPrivateKeyCallback keycb = (UserPrivateKeyCallback) cb;
                        PrivateKey key = lookupUserPrivateKey();
                        keycb.setKey(key);
                    } else {
                        throw new UnsupportedCallbackException(cb);
                    }
                }
            }
        };

        ISecurityContext connectContext = new ISecurityContext() {

            public CallbackHandler getCallbackHandler() {
                return callbackHandler;
            }

        };

        return connectContext;
    }

    protected int getConnectionPort() {
        return OFTP_TEST_PORT;
    }

    protected String getConnectionHost() {
        return OFTP_TEST_HOST;
    }

    protected String getConnectionOid() {
        return OFTP_TEST_OID;
    }

    protected String getConnectionPassword() {
        return OFTP_TEST_PWD;
    }

    protected void runTransfer() throws Exception {
        try {
            client.transfer();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown on transfer() method.");
            throw e;
        }
    }

    protected OdetteFtpClient createClient() {

        String oid = System.getProperty(OFTP_TEST_OID_PROPERTY, getConnectionOid());
        String pwd = System.getProperty(OFTP_TEST_PWD_PROPERTY, getConnectionPassword());

        OdetteFtpClient oclient = new TransientClient(createClientParameters(oid), createSecurityContext(oid, pwd),
                TEST_CLIENT_TMP_DIR);

        return oclient;
    }

    protected void initDirs() throws IOException {
        cleanTmpDirs();
        TEST_CLIENT_TMP_DIR.mkdirs();
    }

    protected void cleanTmpDirs() throws IOException {
        if (TEST_CLIENT_TMP_DIR.exists()) {
            LOGGER.debug("Deleting temp directory: {}", TEST_CLIENT_TMP_DIR.getAbsolutePath());
            IoUtil.deleteDirectory(TEST_CLIENT_TMP_DIR);
        }
    }

}
