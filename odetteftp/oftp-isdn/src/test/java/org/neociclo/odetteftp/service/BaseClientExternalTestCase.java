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
import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_RECORD_SIZE;

import java.io.File;
import java.util.Calendar;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
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
 * @version $Rev$ $Date$
 */
public abstract class BaseClientExternalTestCase {


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

    /** Odette FTP client. */
    protected Client client;

    protected InOutSharedQueueOftpletFactory factory;

    protected Queue<OdetteFtpObject> outgoing;
    protected Queue<OdetteFtpObject> incoming;

    protected Queue<OdetteFtpObject> outgoingDone;

    protected String datasetName;
    protected String originator;
    protected String destination;

    protected abstract Client createClient(OftpletFactory factory);

    protected abstract OdetteFtpConfiguration createSessionConfig();

    @Before
    public void setUp() {

        setRunTests(Boolean.parseBoolean(System.getProperty("oftpExternalTest", "false")));
        if (!isRunTests()) {
            return;
        }

        String userCode = System.getProperty("oftp.userCode");
        String userPswd = System.getProperty("oftp.userPassword");
        String userData = System.getProperty("oftp.userData");

        datasetName = System.getProperty("oftp.dsn");
        originator = System.getProperty("oftp.originator");
        destination = System.getProperty("oftp.destination");

        if (userCode == null || "".equals(userCode.trim())) {
            throw new IllegalArgumentException("oftp.userCode");
        }

        outgoing = new ConcurrentLinkedQueue<OdetteFtpObject>();
        outgoingDone = new ConcurrentLinkedQueue<OdetteFtpObject>();
        incoming = new ConcurrentLinkedQueue<OdetteFtpObject>();

        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

        OdetteFtpConfiguration c = createSessionConfig();
        c.setUserData(userData);

        MappedCallbackHandler callbackHandler = new MappedCallbackHandler();
        callbackHandler.addHandler(PasswordCallback.class, new PasswordHandler(userCode, userPswd));
        
        factory = new InOutSharedQueueOftpletFactory(c, callbackHandler, outgoing, outgoingDone, incoming);

        client = createClient(factory);

    }

	@After
    public void tearDown() {

        if (!runTests) {
            return;
        }

        outgoing = null;
        incoming = null;
        client = null;
    }

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
        } else {
        	vf.setRecordSize(recordSize);
        }

        if (originator != null) {
            vf.setOriginator(originator);
        }

        if (destination != null) {
            vf.setDestination(destination);
        }

        vf.setSize(ProtocolUtil.computeVirtualFileOffset(payload.length(), vf.getRecordFormat(), vf.getRecordSize()));

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

        int randomNumber = Math.abs(RANDOM.nextInt());
        Calendar currentTime = Calendar.getInstance();

        return String.format(pattern, randomNumber, currentTime);
    }

    protected boolean isRunTests() {
		return runTests;
	}

    protected void setRunTests(boolean runTests) {
		this.runTests = runTests;
	}

}
