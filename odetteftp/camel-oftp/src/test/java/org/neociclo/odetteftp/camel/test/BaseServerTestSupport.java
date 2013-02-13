/**
 * Neociclo Accord, Open Source B2B Integration Suite
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
package org.neociclo.odetteftp.camel.test;

import static org.neociclo.odetteftp.examples.server.SimpleServerHelper.*;
import static org.neociclo.odetteftp.protocol.v20.CipherSuite.NO_CIPHER_SUITE_SELECTION;
import static org.neociclo.odetteftp.protocol.v20.FileCompression.NO_COMPRESSION;
import static org.neociclo.odetteftp.protocol.v20.FileEnveloping.NO_ENVELOPE;
import static org.neociclo.odetteftp.protocol.v20.SecurityLevel.NO_SECURITY_SERVICES;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDDSN_FIELD;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.util.ObjectHelper;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.examples.server.SimpleServerOftpletFactory;
import org.neociclo.odetteftp.protocol.DefaultNormalizedVirtualFile;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;
import org.neociclo.odetteftp.protocol.v20.DefaultNormalizedEnvelopedVirtualFile;
import org.neociclo.odetteftp.protocol.v20.EnvelopedVirtualFile;
import org.neociclo.odetteftp.protocol.v20.FileCompression;
import org.neociclo.odetteftp.protocol.v20.FileEnveloping;
import org.neociclo.odetteftp.protocol.v20.ReleaseFormatVer20;
import org.neociclo.odetteftp.protocol.v20.SecurityLevel;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.PasswordAuthenticationCallback;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.service.Server;
import org.neociclo.odetteftp.service.TcpServer;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.support.PasswordHandler;
import org.neociclo.odetteftp.util.IoUtil;
import org.neociclo.odetteftp.util.ProtocolUtil;
import org.neociclo.odetteftp.util.TimestampTicker;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class BaseServerTestSupport extends OftpTestSupport implements IUserManager, IMailboxStoreManager {

	protected String serverOid = "O0055OFTPSERVER";
	protected String serverPwd = "NEOCICLO";
	protected String serverDirName = "server-data";

	protected Server server;

	protected Map<String, AccountInfo> usersRegistry;

	public IUserManager getUserManager() {
		return this;
	}

	public IMailboxStoreManager getMailboxStoreManager() {
		return this;
	}

	@Override
    protected void setUp() throws Exception {

		createDirectory(getOutputDir().getAbsolutePath());
		createDirectory(getTempDir().getAbsolutePath());

		usersRegistry = new HashMap<String, AccountInfo>();
		prepareServerTestData();
		startServer();
    	super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
		shutdownServer();
		usersRegistry = null;
		deleteDirectory(getOutputDir());
    	super.tearDown();
    }

	protected void prepareServerTestData() throws Exception {
	}

	private void startServer() throws Exception {
		initPort();

		createUsersDirectories();

		InetSocketAddress address = new InetSocketAddress(getBindingAddress(), getPort());
		OdetteFtpConfiguration config = getInitialServerConfig();

		MappedCallbackHandler callbackHandler = new MappedCallbackHandler();
		setupSecurityHandlers(callbackHandler);

		SimpleServerOftpletFactory factory = new SimpleServerOftpletFactory(getOutputDir(), config, callbackHandler);
		server = new TcpServer(address, factory);

		server.disableLogging();
		server.start();
	}

	private void createUsersDirectories() {
		Collection<AccountInfo> accounts = getAllAccounts();
		for (AccountInfo a : accounts) {
			createUserDirStructureIfNotExist(a.getUserCode(), getOutputDir());
		}
	}

	protected void setupSecurityHandlers(MappedCallbackHandler callbackHandler) {

		//
		// add server password authentication handler based on the users
		// properties file
		//
		callbackHandler.addHandler(PasswordAuthenticationCallback.class,
				new UserMgrPasswordAuthenticationHandler(this));

		//
		// add password callback which tells the library to reply with server
		// side identification and password
		//
		callbackHandler.addHandler(PasswordCallback.class,
				new PasswordHandler(getServerOid(), getServerPwd()));


	}

	private void shutdownServer() throws Exception {
		server.stop();
		resetPort();
		server = null;
	}

	protected OdetteFtpConfiguration getInitialServerConfig() {
		OdetteFtpConfiguration c = new OdetteFtpConfiguration();
		c.setTransferMode(TransferMode.BOTH);
		return c;
	}

    protected String getBindingAddress() {
    	return "localhost";
    }

    public String getServerOid() {
		return serverOid;
	}

	public String getServerPwd() {
		return serverPwd;
	}

	public String getServerDirName() {
		return serverDirName;
	}

	@Override
	protected File getOutputDir() {
		return new File(super.getOutputDir(), getServerDirName());
	}

	protected File getTempDir() {
		return new File(getOutputDir(), "tmp");
	}

	protected File copyToTempDir(File payloadFile) throws IOException {
		File tmpDir = getTempDir();
		if (!tmpDir.exists()) {
			tmpDir.mkdirs();
		}
		File destFile = new File(tmpDir, payloadFile.getName());
		IoUtil.copy(payloadFile, destFile);
		return destFile;
	}

	// IMailboxStoreManager implementation
	// -------------------------------------------------------------------------

	public void storeDataInMailbox(String mailboxUser, OdetteFtpObject data) throws IOException {

		createUserDirStructureIfNotExist(mailboxUser, getOutputDir());

		if (data instanceof VirtualFile) {
			VirtualFile vf = (VirtualFile) data;
			File payload = vf.getFile();

			ObjectHelper.notEmpty(vf.getDestination(), "destination", vf);
			ObjectHelper.notEmpty(vf.getOriginator(), "originator", vf);

			if (vf instanceof EnvelopedVirtualFile) {
				vf = normalizeEnvelopedVirtualFile(vf);
			} else {
				vf = normalizeVirtualFile(vf);
			}

			File dataFile = createDataFile(vf, getOutputDir());
			IoUtil.copy(payload, dataFile);

			data = vf;
		}

		storeInMailbox(mailboxUser, data, getOutputDir());
		
	}

    private EnvelopedVirtualFile normalizeEnvelopedVirtualFile(VirtualFile vf) {

        String dsn = (vf.getDatasetName() == null ? (vf.getFile() == null ? null : vf.getFile().getName()) : vf
                .getDatasetName());
        Date dateTime = (vf.getDateTime() == null ? (vf.getFile() == null ? null
                : new Date(vf.getFile().lastModified())) : vf.getDateTime());

        if (dsn == null) {
            throw new NullPointerException("Enveloped Virtual File object has null Dataset Name");
        } else if (dateTime == null) {
            throw new NullPointerException("Enveloped Virtual File object has null Date/Time");
        }

        // set API's generated timestamp counter (ticker) if empty
        Short ticker = vf.getTicker();
        if (ticker == null) {
        	ticker = Short.valueOf((short) TimestampTicker.getInstance().incrementAndGet());
        } else {
    		if (ticker > TimestampTicker.MAX_COUNTER_VALUE) {
    			ticker = 1;
    		}
        }

        int dsnLength = ReleaseFormatVer20.SFID_V20.getField(SFIDDSN_FIELD).getSize();
        if (dsn.length() > dsnLength) {
        	dsn = dsn.substring(0, dsnLength);
        }

        String dest = vf.getDestination();
        String orig = vf.getOriginator();

        RecordFormat recordFormat = (vf.getRecordFormat() == null ? RecordFormat.UNSTRUCTURED : vf.getRecordFormat());
        int recordSize = (recordFormat == RecordFormat.UNSTRUCTURED || recordFormat == RecordFormat.TEXTFILE ? 0 : Math
                .max(vf.getRecordSize(), 0));
        long restartOffset = Math.max(vf.getRestartOffset(), 0);

        long unitCount = (vf.getFile() == null ? 0 : vf.getFile().length());
        long fileSize = Math.max(vf.getSize(), ProtocolUtil.computeVirtualFileSize(unitCount, recordFormat, recordSize));

        //
        // Default OFTP2 start file values when a simple VirtualFile object
        // used to send the payload instead of an EnvelopedVirtualFile
        //

        FileCompression compressionAlgorithm = NO_COMPRESSION;
        long originalFileSize = fileSize;
        SecurityLevel securityLevel = NO_SECURITY_SERVICES;
        CipherSuite cipherSuite = NO_CIPHER_SUITE_SELECTION;
        FileEnveloping enveloping = NO_ENVELOPE;
        boolean signedNotifRequest = false;
        String fileDescription = null;


        if (vf instanceof EnvelopedVirtualFile) {
            EnvelopedVirtualFile env = (EnvelopedVirtualFile) vf;

            compressionAlgorithm = (env.getCompressionAlgorithm() == null ? NO_COMPRESSION : env
                    .getCompressionAlgorithm());
            originalFileSize = Math.max(env.getOriginalFileSize(), fileSize);
            securityLevel = (env.getSecurityLevel() == null ? NO_SECURITY_SERVICES : env.getSecurityLevel());
            cipherSuite = (env.getCipherSuite() == null ? NO_CIPHER_SUITE_SELECTION : env.getCipherSuite());
            enveloping = (env.getEnvelopingFormat() == null ? NO_ENVELOPE : env.getEnvelopingFormat());
            signedNotifRequest = env.isSignedNotificationRequest();
            fileDescription = env.getFileDescription();

        }

        // return the normalized virtual file
        DefaultNormalizedEnvelopedVirtualFile n = new DefaultNormalizedEnvelopedVirtualFile(vf);
        n.setDatasetName(dsn);
        n.setDateTime(dateTime);
        n.setDestination(dest);
        n.setOriginator(orig);
        n.setTicker(ticker);
        n.setRecordFormat(recordFormat);
        n.setRecordSize(recordSize);
        n.setSize(fileSize);
        n.setRestartOffset(restartOffset);

        n.setFile(vf.getFile());

        n.setEnvelopingFormat(enveloping);
        n.setCipherSuite(cipherSuite);
        n.setSecurityLevel(securityLevel);
        n.setOriginalFileSize(originalFileSize);
        n.setCompressionAlgorithm(compressionAlgorithm);

        n.setSignedNotificationRequest(signedNotifRequest);
        n.setFileDescription(fileDescription);

        return n;

    }

	private DefaultNormalizedVirtualFile normalizeVirtualFile(VirtualFile vf) {

        String dsn = (vf.getDatasetName() == null ? (vf.getFile() == null ? null : vf.getFile().getName()) : vf
                .getDatasetName());
		Date dateTime = (vf.getDateTime() == null ? (vf.getFile() == null ? null
				: new Date(vf.getFile().lastModified())) : vf.getDateTime());

        if (dsn == null) {
            throw new NullPointerException("Virtual File object has null Dataset Name");
        } else if (dateTime == null) {
            throw new NullPointerException("Virtual File object has null Date/Time");
        }

        int dsnLength = ReleaseFormatVer13.SFID_V13.getField(SFIDDSN_FIELD).getSize();
        if (dsn.length() > dsnLength) {
        	dsn = dsn.substring(0, dsnLength);
        }

        String dest = vf.getDestination();
        String orig = vf.getOriginator();

        RecordFormat recordFormat = (vf.getRecordFormat() == null ? RecordFormat.UNSTRUCTURED : vf.getRecordFormat());
        int recordSize = (recordFormat == RecordFormat.UNSTRUCTURED || recordFormat == RecordFormat.TEXTFILE ? 0 : Math
                .max(vf.getRecordSize(), 0));
        long restartOffset = Math.max(vf.getRestartOffset(), 0);

        long unitCount = (vf.getFile() == null ? 0 : vf.getFile().length());
        long fileSize = Math.max(vf.getSize(), ProtocolUtil.computeVirtualFileSize(unitCount, recordFormat, recordSize));

        // return the normalized virtual file
        DefaultNormalizedVirtualFile n = new DefaultNormalizedVirtualFile(vf);
        n.setDatasetName(dsn);
        n.setDateTime(dateTime);
        n.setDestination(dest);
        n.setOriginator(orig);
        n.setRecordFormat(recordFormat);
        n.setRecordSize(recordSize);
        n.setSize(fileSize);
        n.setRestartOffset(restartOffset);

        n.setFile(vf.getFile());

        return n;
	}

	public boolean hasDataInMailbox(String mailboxUser) {
		return hasExchange(mailboxUser, getOutputDir());
	}

	public OdetteFtpObject[] listDataInMailbox(String mailboxUser) throws IOException {
		File[] files = listExchanges(mailboxUser, getOutputDir());
		ArrayList<OdetteFtpObject> exchanges = new ArrayList<OdetteFtpObject>();
		if (files != null) {
			for (File f : files) {
				OdetteFtpObject o = loadObject(f);
				exchanges.add(o);
			}
		}
		return exchanges.toArray(new OdetteFtpObject[exchanges.size()]);
	}

	public void removeDataFromMailbox(String mailboxUser, OdetteFtpObject data) {
		deleteExchange(mailboxUser, data, getOutputDir());
	}

	// IUserManager implementation
	// -------------------------------------------------------------------------

	public AccountInfo addAccount(AccountInfo account) {
		synchronized (usersRegistry) {
			if (account == null) {
				throw new NullPointerException("account");
			}
			return usersRegistry.put(account.getUserCode().toUpperCase(), account);
		}
	}

	public AccountInfo removeAccount(String userCode) {
		synchronized (usersRegistry) {
			return usersRegistry.remove(userCode.toUpperCase());
		}
	}

	public AccountInfo getAccount(String userCode) {
		synchronized (usersRegistry) {
			return usersRegistry.get(userCode);
		}
	}

	public Collection<AccountInfo> getAllAccounts() {
		synchronized (usersRegistry) {
			return usersRegistry.values();
		}
	}


}
