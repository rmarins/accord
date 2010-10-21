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

import org.jboss.netty.util.internal.ExecutorUtil;
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
import org.neociclo.odetteftp.util.ProtocolUtil;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
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

        String host = System.getProperty("oftp.server");
        String port = System.getProperty("oftp.port");
        boolean useSsl = Boolean.parseBoolean(System.getProperty("oftp.ssl", "false"));

        String userCode = System.getProperty("oftp.userCode");
        String userPswd = System.getProperty("oftp.userPassword");
        String userData = System.getProperty("oftp.userData");

        datasetName = System.getProperty("oftp.dsn");
        originator = System.getProperty("oftp.originator");
        destination = System.getProperty("oftp.destination");

        if (host == null || "".equals(host.trim())) {
            throw new IllegalArgumentException("oftp.server");
        } else if (userCode == null || "".equals(userCode.trim())) {
            throw new IllegalArgumentException("oftp.userCode");
        }

        int portNum = Integer.parseInt(port);
        if (portNum <= 0) {
            portNum = (useSsl ? DEFAULT_SECURE_OFTP_PORT : DEFAULT_OFTP_PORT);
        }

        outgoing = new ConcurrentLinkedQueue<OdetteFtpObject>();
        outgoingDone = new ConcurrentLinkedQueue<OdetteFtpObject>();
        incoming = new ConcurrentLinkedQueue<OdetteFtpObject>();

        OdetteFtpConfiguration c = createSessionConfig();
        c.setUserData(userData);

        MappedCallbackHandler callbackHandler = new MappedCallbackHandler();
        callbackHandler.addHandler(PasswordCallback.class, new PasswordHandler(userCode, userPswd));
        
        factory = new InOutSharedQueueOftpletFactory(c, callbackHandler, outgoing, outgoingDone, incoming);

        client = new TcpClient(host, portNum, factory);

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

        vf.setSize(ProtocolUtil.computeVirtualFileSize(payload.length(), vf.getRecordFormat(), vf.getRecordSize()));

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
