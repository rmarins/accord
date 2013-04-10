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

import static org.neociclo.odetteftp.protocol.RecordFormat.FIXED;
import static org.neociclo.odetteftp.protocol.RecordFormat.UNSTRUCTURED;
import static org.neociclo.odetteftp.protocol.RecordFormat.VARIABLE;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_OFTP_PORT;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_RECORD_SIZE;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_SECURE_OFTP_PORT;

import java.io.File;
import java.util.Calendar;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.support.InOutSharedQueueOftpletFactory;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.support.PasswordHandler;
import org.neociclo.odetteftp.util.ExecutorUtil;
import org.neociclo.odetteftp.util.ProtocolUtil;

/**
 * @author Rafael Marins
 */
public abstract class AbstractTcpClientExternal {

    private static final Random RANDOM = new Random();

    private static Executor executor;

    @BeforeClass
    public static void init() {
        executor = Executors.newCachedThreadPool();
    }

    @AfterClass
    public static void destroy() {
        ExecutorUtil.terminate(executor);
    }

    protected boolean runTests;

    /** Odette FTP client (for TCP/IP). */
    protected TcpClient client;

    protected InOutSharedQueueOftpletFactory factory;

    protected Queue<OdetteFtpObject> outgoing;
    protected Queue<OdetteFtpObject> incoming;

    protected Queue<OdetteFtpObject> outgoingDone;

    protected String datasetName;
    protected String originator;
    protected String destination;
    
    @Before
    public void beginTest() {

        runTests = Boolean.parseBoolean(System.getProperty("oftp.tcp.test", "false"));
        if (!runTests) {
            return;
        }

        String userCode = System.getProperty("oftp.userCode");
        String userPswd = System.getProperty("oftp.userPassword");
        String userData = System.getProperty("oftp.userData");

        if (userCode == null || "".equals(userCode.trim())) {
            throw new IllegalArgumentException("oftp.userCode");
        }

        datasetName = System.getProperty("oftp.dsn");
        originator = System.getProperty("oftp.originator");
        destination = System.getProperty("oftp.destination");

        outgoing = new ConcurrentLinkedQueue<OdetteFtpObject>();
        outgoingDone = new ConcurrentLinkedQueue<OdetteFtpObject>();
        incoming = new ConcurrentLinkedQueue<OdetteFtpObject>();

        OdetteFtpConfiguration c = createSessionConfig();
        c.setUserData(userData);

        MappedCallbackHandler callbackHandler = new MappedCallbackHandler();
        callbackHandler.addHandler(PasswordCallback.class, new PasswordHandler(userCode, userPswd));
        
        factory = new InOutSharedQueueOftpletFactory(c, callbackHandler, outgoing, outgoingDone, incoming);

        client = new TcpClient();
        client.setOftpletFactory(factory);

    }

    protected void connect() throws Exception {
    	connect(true);
    }

    protected void connect(boolean await) throws Exception {

    	String host = System.getProperty("oftp.server");
        String port = System.getProperty("oftp.port");
        boolean useSsl = Boolean.parseBoolean(System.getProperty("oftp.ssl", "false"));

        if (host == null || "".equals(host.trim())) {
            throw new IllegalArgumentException("oftp.server");
        }

        int portNum = Integer.parseInt(port);
        if (portNum <= 0) {
            portNum = (useSsl ? DEFAULT_SECURE_OFTP_PORT : DEFAULT_OFTP_PORT);
        }

        client.connect(host, portNum, await);
    }

    @After
    public void endTest() {

        if (!runTests) {
            return;
        }

        outgoing = null;
        incoming = null;
        client = null;
    }

    protected abstract OdetteFtpConfiguration createSessionConfig();

    protected VirtualFile createVirtualFile(File payload) {
        String dsn = (datasetName == null ? payload.getName() : datasetName);
        return createVirtualFile(dsn, null, 0, 0, payload);
    }

    /**
     * 
     * @param dsnPattern
     *            Dataset Name pattern used to format using
     *            {@link String#format(String, Object...)}, considering the
     *            arguments: 1) random number, 2) current time
     * @param recordFormat
     * @param recordSize
     * @param restartOffset
     * @param payload
     * @return
     */
    protected VirtualFile createVirtualFile(String dsnPattern, RecordFormat recordFormat, int recordSize, long restartOffset, File payload) {

        String dsn = createDatasetName(dsnPattern);
        Calendar currentTime = Calendar.getInstance();

        DefaultVirtualFile vf = new DefaultVirtualFile();
        vf.setFile(payload);

        vf.setDatasetName(dsn);
        vf.setDateTime(currentTime.getTime());

        if (recordFormat == null) {
            vf.setRecordFormat(UNSTRUCTURED);
        } else {
            vf.setRecordFormat(recordFormat);
        }

        if (recordSize <= 0 && (vf.getRecordFormat() == FIXED || vf.getRecordFormat() == VARIABLE)) {
            vf.setRecordSize(DEFAULT_RECORD_SIZE);
        }

        if (originator != null) {
            vf.setOriginator(originator);
        }

        if (destination != null) {
            vf.setDestination(destination);
        }

        vf.setSize(ProtocolUtil.computeVirtualFileSize(payload.length()));

        return vf;
    }

    /**
     * 
     * @param pattern
     * @return
     * @see java.util.Formatter
     */
    protected String createDatasetName(String pattern) {

        if (pattern == null) {
            throw new NullPointerException("pattern");
        }

        String randomNumber = String.valueOf(Math.abs(RANDOM.nextInt()));
        Calendar currentTime = Calendar.getInstance();

        return String.format(pattern, randomNumber, currentTime);
    }


}
