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
package org.neociclo.odetteftp.examples.server;

import static org.neociclo.odetteftp.examples.server.SimpleServerHelper.*;
import static org.neociclo.odetteftp.protocol.DefaultEndFileResponse.*;
import static org.neociclo.odetteftp.protocol.DefaultStartFileResponse.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
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
import org.neociclo.odetteftp.security.DefaultSecurityContext;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.SecurityContext;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.support.OftpletEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 */
class SimpleServerOftplet extends OftpletAdapter implements org.neociclo.odetteftp.oftplet.ServerOftplet, OftpletSpeaker, OftpletListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleServerOftplet.class);

	private static final SimpleServerRoutingWorker ROUTING_WORKER = new SimpleServerRoutingWorker();

	private File serverBaseDir;
	private OftpletEventListener listener;
	private SecurityContext securityContext;
	private OdetteFtpConfiguration config;
	private OdetteFtpSession session;

	private Map<String, Iterator<File>> outFileIteratorMap = new HashMap<String, Iterator<File>>();

	public SimpleServerOftplet(File serverBaseDir, OdetteFtpConfiguration config, MappedCallbackHandler securityCallbackHandler, OftpletEventListener listener) {
		super();
		this.serverBaseDir = serverBaseDir;
		this.config = config;
		this.securityContext = new DefaultSecurityContext(securityCallbackHandler);
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

		Iterator<File> filesIt = getUserOutFileIterator(userCode);
		if (filesIt.hasNext()) {
			File cur = filesIt.next();
			try {
				next = loadObject(cur);
			} catch (IOException e) {
				LOGGER.error("Failed to load Odette FTP obejct file: " + cur, e);
				if (cur.exists()) {
					cur.delete();
				}
			}
		} else {
			outFileIteratorMap.remove(userCode);
		}

		return next;
	}

	private Iterator<File> getUserOutFileIterator(String userCode) {
		Iterator<File> it = outFileIteratorMap.get(userCode);
		if (it == null) {
			File[] a = listExchanges(userCode);
			List<File> files = Arrays.asList(a);
			it = files.iterator();
			outFileIteratorMap.put(userCode, it);
		}
		return it;
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
			LOGGER.trace("Saving to: {}", dataFile);
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
		SimpleServerHelper.storeInWork(userCode, obj, serverBaseDir);
	}

	private void createUserDirStructureIfNotExist(String userCode) {
		SimpleServerHelper.createUserDirStructureIfNotExist(userCode, serverBaseDir);
	}

	private boolean recipientExists(String userCode, String recipientOid) {
		File recipientDir = getUserDir(serverBaseDir, recipientOid);
		return recipientDir.exists();
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
		return SimpleServerHelper.createDataFile(vf, serverBaseDir);
	}

	/**
	 * Check it has exchange in the user mailbox.
	 *
	 * @param userCode
	 * @return
	 */
	private boolean hasExchange(String userCode) {
		return SimpleServerHelper.hasExchange(userCode, serverBaseDir);
	}

	private File[] listExchanges(String userCode) {
		return SimpleServerHelper.listExchanges(userCode, serverBaseDir);
	}

	private void deleteExchange(OdetteFtpObject obj) {
		String userCode = session.getUserCode();
		SimpleServerHelper.deleteExchange(userCode, obj, serverBaseDir);
	}

}
