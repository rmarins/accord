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
package org.neociclo.odetteftp.examples.server;

import static org.neociclo.odetteftp.examples.server.SimpleServerHelper.*;
import static org.neociclo.odetteftp.protocol.DefaultStartFileResponse.*;
import static org.neociclo.odetteftp.protocol.DefaultEndFileResponse.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.examples.support.DefaultSecurityContext;
import org.neociclo.odetteftp.examples.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.examples.support.PropertiesBasedConfiguration;
import org.neociclo.odetteftp.oftplet.AnswerReasonInfo;
import org.neociclo.odetteftp.oftplet.EndFileResponse;
import org.neociclo.odetteftp.oftplet.OftpletAdapter;
import org.neociclo.odetteftp.oftplet.OftpletListener;
import org.neociclo.odetteftp.oftplet.OftpletSpeaker;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.AnswerReason;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.security.SecurityContext;
import org.neociclo.odetteftp.support.OftpletEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
class SimpleServerOftplet extends OftpletAdapter implements org.neociclo.odetteftp.oftplet.ServerOftplet, OftpletSpeaker, OftpletListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleServerOftplet.class);

	private static final SimpleServerRoutingWorker ROUTING_WORKER = new SimpleServerRoutingWorker();

	private File serverBaseDir;
	private OftpletEventListener listener;
	private SecurityContext securityContext;
	private OdetteFtpConfiguration config;
	private OdetteFtpSession session;

	public SimpleServerOftplet(File serverBaseDir, OdetteFtpConfiguration config, OftpletEventListener listener) {
		super();
		this.serverBaseDir = serverBaseDir;
		this.config = config;
		this.listener = listener;
	}

	// -------------------------------------------------------------------------
	//   Oftplet implementation
	// -------------------------------------------------------------------------

	@Override
	public boolean isProtocolVersionSupported(OdetteFtpVersion version) {
		// server that accepts downgrading the version
		return (config != null ? config.getVersion().isEqualOrOlder(version) : super.isProtocolVersionSupported(version));
	};

	@Override
	public SecurityContext getSecurityContext() {
		if (securityContext == null) {
			securityContext = new DefaultSecurityContext(config.getCallbackHandler());
		}
		return securityContext;
	}

	@Override
	public void init(OdetteFtpSession session) throws OdetteFtpException {
		this.session = session;
		config.setup(session);

		if (listener != null) {
			listener.init(session);
		}
	}

	public void configure() {

		// setup custom parameters specific to this user configuration
		String userCode = session.getUserCode();
		File configFile = getUserConfigFile(serverBaseDir, userCode);
		PropertiesBasedConfiguration customConfig = new PropertiesBasedConfiguration();

		try {
			customConfig.load(new FileInputStream(configFile));
			customConfig.setup(session);
		} catch (IOException e) {
			LOGGER.error("Cannot load user's custom configuration.", e);
		}

		if (listener != null) {
			listener.configure(session);
		}
	}

	@Override
	public void destroy() {
		this.config = null;
		this.session = null;
		this.securityContext = null;
		if (listener != null) {
			listener.destroy();
		}
	}

	@Override
	public void onSessionStart() {

		String userCode = session.getUserCode();
		createUserDirStructureIfNotExist(userCode);

		if (listener != null) {
			listener.onSessionStart();
		}
	}

	@Override
	public void onExceptionCaught(Throwable cause) {
		LOGGER.error("Exception Caught.", cause);
		if (listener != null) {
			listener.onExceptionCaught(cause);
		}
	}

	@Override
	public void onSessionEnd() {
		if (listener != null) {
			listener.onSessionEnd();
		}
	}

	@Override
	public OftpletSpeaker getSpeaker() {
		return this;
	}

	@Override
	public OftpletListener getListener() {
		return this;
	}

	// -------------------------------------------------------------------------
	//   OftpletSpeaker implementation
	// -------------------------------------------------------------------------

	public OdetteFtpObject nextOftpObjectToSend() {

		OdetteFtpObject next = null;

		String userCode = session.getUserCode();
		File[] exchanges = listExchanges(userCode);

		if (exchanges.length > 0) {
			File cur = exchanges[0]; 
			try {
				next = loadObject(cur);
			} catch (IOException e) {
				LOGGER.error("Failed to load Odette FTP obejct file: " + cur, e);
				if (cur.exists()) {
					cur.delete();
				}
			}
		}

		return next;

	}

	public void onSendFileStart(VirtualFile virtualFile, long answerCount) {
	}

	public void onDataSent(VirtualFile virtualFile, long totalOctetsSent) {
	}

	public void onSendFileEnd(VirtualFile virtualFile) {
		deleteExchange(virtualFile);
	}

	public void onSendFileError(VirtualFile virtualFile, AnswerReasonInfo reason, boolean retryLater) {
	}

	public void onNotificationSent(DeliveryNotification notif) {
		deleteExchange(notif);
	}

	// -------------------------------------------------------------------------
	//   OftpletListener implementation
	// -------------------------------------------------------------------------

	public StartFileResponse acceptStartFile(VirtualFile vf) {

		String userCode = session.getUserCode();
		String recipientOid = vf.getDestination();

		if (!recipientExists(userCode, recipientOid)) {
			return negativeStartFileAnswer(AnswerReason.INVALID_DESTINATION,
					"Recipient [" + recipientOid + "] doesn't exist.", false);
		}
		createUserDirStructureIfNotExist(recipientOid);

		if (targetFileExists(recipientOid, vf)) {
			return negativeStartFileAnswer(AnswerReason.DUPLICATE_FILE,
					"File already exist in the recipient [" + recipientOid + "].", true);
		}

		File dataFile = null;
		try {
			dataFile = createDataFile(vf);
		} catch (IOException e) {
			LOGGER.error("Cannot create data file for object: " + vf, e);
			return negativeStartFileAnswer(AnswerReason.ACCESS_METHOD_FAILURE, "Couldn't store file in local system.",
					true);
		}
		return positiveStartFileAnswer(dataFile);
	}

	public void onReceiveFileStart(VirtualFile virtualFile, long answerCount) {

		try {
			store(virtualFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void onDataReceived(VirtualFile virtualFile, long totalOctetsReceived) {
	}

	public EndFileResponse onReceiveFileEnd(VirtualFile virtualFile, long recordCount, long unitCount) {

		String userCode = session.getUserCode();
		ROUTING_WORKER.deliver(serverBaseDir, userCode, virtualFile);

		return positiveEndFileAnswer(hasExchange(userCode));
	}

	public void onReceiveFileError(VirtualFile virtualFile, AnswerReasonInfo reason) {
	}

	public void onNotificationReceived(DeliveryNotification notif) {

		String userCode = session.getUserCode();

		try {
			store(notif);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ROUTING_WORKER.deliver(serverBaseDir, userCode, notif);

	}

	// -------------------------------------------------------------------------
	//   Implementation specific methods
	// -------------------------------------------------------------------------

	private void store(OdetteFtpObject obj) throws IOException {

		String userCode = session.getUserCode();

		File workDir = getUserWorkDir(serverBaseDir, userCode);
		String filename = createFileName(obj);

		File outputFile = new File(workDir, filename);
		storeObject(outputFile, obj);

	}

	private void createUserDirStructureIfNotExist(String userCode) {

		File dataDir = getServerDataDir(serverBaseDir);
		File mailboxDir = getUserMailboxDir(serverBaseDir, userCode);
		File workDir = getUserWorkDir(serverBaseDir, userCode);

		if (!dataDir.exists()) {
			dataDir.mkdirs();
		}

		if (!mailboxDir.exists()) {
			mailboxDir.mkdirs();
		}

		if (!workDir.exists()) {
			workDir.mkdirs();
		}

	}

	private boolean recipientExists(String userCode, String recipientOid) {
		File recipientConf = getUserConfigFile(serverBaseDir, recipientOid);
		return recipientConf.exists();
	}

	/**
	 * Check if the Virtual File already exist in the recipient mailbox.
	 * 
	 * @param recipientOid
	 * @param vf
	 * @return
	 */
	private boolean targetFileExists(String recipientOid, VirtualFile vf) {

		String filename = createFileName(vf);
		File mailboxDir = getUserMailboxDir(serverBaseDir, recipientOid);

		File target = new File(mailboxDir, filename);
		return target.exists();
	}

	private File createDataFile(VirtualFile vf) throws IOException {
		String filename = createFileName(vf);
		File dataDir = getServerDataDir(serverBaseDir);
		return File.createTempFile(filename + "_", null, dataDir);
	}

	/**
	 * Check it has exchange in the user mailbox.
	 *
	 * @param userCode
	 * @return
	 */
	private boolean hasExchange(String userCode) {
		File[] exchanges = listExchanges(userCode);
		return (exchanges != null && exchanges.length > 0);
	}

	private File[] listExchanges(String userCode) {
		File mailboxDir = getUserMailboxDir(serverBaseDir, userCode);
		File[] exchanges = mailboxDir.listFiles(EXCHANGES_FILENAME_FILTER);
		return exchanges;
	}

	private void deleteExchange(OdetteFtpObject obj) {
		
		if (obj instanceof VirtualFile) {
			VirtualFile vf = (VirtualFile) obj;
			File payloadFile = vf.getFile();
			if (payloadFile.exists()) {
				payloadFile.delete();
			}
		}

		String userCode = session.getUserCode();
		File mailboxDir = getUserMailboxDir(serverBaseDir, userCode);
		String filename = createFileName(obj);
		File mailboxFile = new File(mailboxDir, filename);

		if (mailboxFile.exists()) {
			mailboxFile.delete();
		}
	}

}
