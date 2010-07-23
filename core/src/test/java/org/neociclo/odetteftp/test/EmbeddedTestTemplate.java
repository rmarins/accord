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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.neociclo.odetteftp.support.TransportType.TCPIP;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.mina.filter.logging.LogLevel;
import org.junit.After;
import org.junit.Before;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.client.ClientParameters;
import org.neociclo.odetteftp.client.OdetteFtpClient;
import org.neociclo.odetteftp.client.transfer.TransientClient;
import org.neociclo.odetteftp.protocol.VirtualFileInfo;
import org.neociclo.odetteftp.security.ISecurityContext;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.service.Server;
import org.neociclo.odetteftp.service.ServiceConfiguration;
import org.neociclo.odetteftp.support.TransportType;
import org.neociclo.odetteftp.util.IoUtil;
import org.neociclo.odetteftp.util.OftpTestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public abstract class EmbeddedTestTemplate {

    public static final int OFTP_TESTING_PORT = 13305;

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedTestTemplate.class);

    private static final File MAILBOXES_FILE = new File(OftpTestUtil.getBaseDir(),
            "src/test/resources/mailboxes.properties");

    private static final File TEST_SERVICE_TMP_DIR = new File("test-service-tmp");

    private static final File TEST_CLIENT_TMP_DIR = new File("test-client-tmp");

    private static final boolean IS_SERVER_LOGGING_ENABLED = true;

    protected static Thread cleanTempDirsThread;

    protected OdetteFtpClient client;

    protected Server server;

    public EmbeddedTestTemplate() {
        super();

        if (cleanTempDirsThread == null) {
            LOGGER.debug("EmbeddedTestTemplate activated shutdownHook callback to clean up temp directories.");
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

    protected boolean isStartService() {
        return true;
    }

    @Before
    public void setUp() throws Exception {
        initDirs();
        initServer();
    }

    @After
    public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }

    }

    protected ClientParameters createClientParameters(String oid) {

        ServiceConfiguration config = createServiceConfiguration();

        ClientParameters connectConfig = new ClientParameters();

        connectConfig.setHost("localhost");
        connectConfig.setPort(getServicePort());
        connectConfig.setOid(oid);
        connectConfig.setVersion(getTestResponderVersion());

        connectConfig.setTransport(TCPIP);

        connectConfig.setSsl(config.isSsl());
        connectConfig.setLoggingEnabled(!config.isLoggingEnabled());
        connectConfig.setLoggingLevel("DEBUG");

        return connectConfig;
    }

    protected VirtualFileInfo createMappingInfo(File payload) {
        VirtualFileInfo info = new VirtualFileInfo();
        info.setDatasetName(payload.getName());
        info.setDateTime(Calendar.getInstance().getTime());
        return info;
    }

    protected ISecurityContext createSecurityContext(final String oid, final String pwd) {

        final CallbackHandler callbackHandler = new javax.security.auth.callback.CallbackHandler() {
            public void handle(javax.security.auth.callback.Callback[] callbacks) throws IOException,
                    UnsupportedCallbackException {
                for (Callback cb : callbacks) {
                    if (cb instanceof PasswordCallback) {
                        PasswordCallback pwdcb = (PasswordCallback) cb;
                        if (oid.equalsIgnoreCase(pwdcb.getPrompt())) {
                            pwdcb.setPassword(pwd);
                        }
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

    protected int getServicePort() {
        return OFTP_TESTING_PORT;
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

    protected OdetteFtpClient createClient(String oid, String pwd) {

        OdetteFtpClient oclient = new TransientClient(createClientParameters(oid), createSecurityContext(oid, pwd),
                TEST_CLIENT_TMP_DIR);

        return oclient;
    }

    protected boolean isConnectClient() {
        return true;
    }

    protected ServiceConfiguration createServiceConfiguration() {
        ServiceConfiguration config = new ServiceConfiguration();
        config.setPort(getServicePort());
        config.setTransport(TransportType.TCPIP);
        config.setLoggingEnabled(IS_SERVER_LOGGING_ENABLED);
        config.setLoggingLevel(LogLevel.DEBUG.name());
        return config;
    }

    protected Server createListener() {
        assertTrue(MAILBOXES_FILE.getAbsolutePath() + " must exist", MAILBOXES_FILE.exists());

        ServiceConfiguration config = createServiceConfiguration();

        PropertiesMailboxManagerFactory mmFactory = new PropertiesMailboxManagerFactory();
        mmFactory.setFile(MAILBOXES_FILE);
        MailboxManager mbxmgr = mmFactory.createMailboxManager();

        assertNotNull("MailboxManager instance wasn't created by its the factory.", mbxmgr);

        mbxmgr.setIngoreCasePassword(true);

        BasicOftpletFactory oftpletFactory = new BasicOftpletFactory(TEST_SERVICE_TMP_DIR, mbxmgr, TransferMode.BOTH);

        return new Server(config, oftpletFactory, getTestResponderVersion());
    }

    protected void initServer() throws Exception {
        server = createListener();
        if (isStartService()) {
            server.start();
        }
    }

    protected void initDirs() throws IOException {
        cleanTmpDirs();
        TEST_SERVICE_TMP_DIR.mkdirs();
        TEST_CLIENT_TMP_DIR.mkdirs();
    }

    protected void cleanTmpDirs() throws IOException {
        if (TEST_SERVICE_TMP_DIR.exists()) {
            LOGGER.debug("Deleting temp directory: {}", TEST_SERVICE_TMP_DIR.getAbsolutePath());
            IoUtil.deleteDirectory(TEST_SERVICE_TMP_DIR);
        }
        if (TEST_CLIENT_TMP_DIR.exists()) {
            LOGGER.debug("Deleting temp directory: {}", TEST_CLIENT_TMP_DIR.getAbsolutePath());
            IoUtil.deleteDirectory(TEST_CLIENT_TMP_DIR);
        }
    }

}
