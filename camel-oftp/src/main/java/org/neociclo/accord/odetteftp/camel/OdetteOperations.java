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
package org.neociclo.accord.odetteftp.camel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLEngine;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.file.FileOperations;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.util.IOHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.oftplet.AnswerReasonInfo;
import org.neociclo.odetteftp.oftplet.EndFileResponse;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DefaultEndFileResponse;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.InOutSharedQueueOftpletFactory;
import org.neociclo.odetteftp.support.OftpletEventListener;
import org.neociclo.odetteftp.support.SessionConfig;

public class OdetteOperations implements OftpletEventListener {

	private static final long TEMP_COPY_BUFFER_SIZE = 128 * 1024;
	protected final transient Log log = LogFactory.getLog(getClass());
	private OdetteEndpoint endpoint;
	private TcpClient client;
	private Queue<OdetteFtpObject> outgoingQueue = new ConcurrentLinkedQueue<OdetteFtpObject>();
	private Queue<OdetteFtpObject> incomingQueue = new ConcurrentLinkedQueue<OdetteFtpObject>();
	private boolean hasOut;
	private boolean hasIn;
	private InOutSharedQueueOftpletFactory factory;
	private Set<String> temporaryFiles = new HashSet<String>();
	private Map<VirtualFile, Exchange> lockedOutgoingQueue = new Hashtable<VirtualFile, Exchange>();
	private volatile Exchange exchangeInTransit;
	private SSLEngine sslEngine;

	public OdetteOperations(OdetteEndpoint odetteEndpoint) throws Exception {
		this.endpoint = odetteEndpoint;

		initSSLEngine();
	}

	public boolean isConnected() {
		if (client != null) {
			return client.isConnected();
		}

		return false;
	}

	/**
	 * <p>
	 * Starts polling files and notifications from Odette server. If Producer
	 * has added any file/notification to be sent, will do during this session.
	 * </p>
	 * 
	 * @throws Exception
	 * 
	 * @throws ClientException
	 */
	public void pollServer() throws Exception {
		if (client == null || !client.isConnected()) {
			initClient();

			final OdetteConfiguration cfg = endpoint.getConfiguration();

			client = new TcpClient(cfg.getHost(), cfg.getPort(), sslEngine, factory);
			client.connect(true);
		}
	}

	private SSLEngine initSSLEngine() throws Exception {
		OdetteConfiguration cfg = endpoint.getConfiguration();
		if (!cfg.isSsl()) {
			return null;
		}

		SSLEngineFactory userEngineFactory = cfg.getSslEngineFactory();
		if (userEngineFactory == null) {
			userEngineFactory = new SSLEngineFactory().setup(cfg.getKeyStoreFormat(), cfg.getSecurityProvider(),
					cfg.getKeyStoreFile(), cfg.getTrustStoreFile(), cfg.getPassphrase().toCharArray());
		}

		return userEngineFactory.createClientSSLEngine();
	}

	private void initClient() {
		final OdetteConfiguration cfg = endpoint.getConfiguration();

		TransferMode identifyTransferMode = identifyTransferMode();

		SessionConfig session = new SessionConfig();
		session.setUserCode(cfg.getOid());
		session.setUserPassword(cfg.getPassword());
		session.setTransferMode(identifyTransferMode);
		session.setDataExchangeBufferSize(cfg.getBufferSize());
		session.setWindowSize(cfg.getWindowSize());

		factory = new InOutSharedQueueOftpletFactory(session, outgoingQueue, null, incomingQueue);
		factory.setEventListener(this);
	}

	private TransferMode identifyTransferMode() {
		if (hasIn && hasOut)
			return TransferMode.BOTH;
		else if (hasIn)
			return TransferMode.RECEIVER_ONLY;
		else if (hasOut)
			return TransferMode.SENDER_ONLY;

		return null;
	}

	public void awaitDisconnect() {
		if (client != null) {
			try {
				client.awaitDisconnect();
			} catch (Exception e) {
			}
		}
	}

	public void setHasInQueue() {
		hasIn = true;
	}

	public void setHasOutQueue() {
		hasOut = true;
	}

	public void offer(DeliveryNotification notification) {
		outgoingQueue.offer(notification);
		offerQueueToClient();
	}

	public void offer(VirtualFile payload, Exchange lockable) {
		outgoingQueue.offer(payload);
		offerQueueToClient();

		synchronized (lockable) {
			try {
				lockedOutgoingQueue.put(payload, lockable);
				lockable.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected Exchange findLockedExchange(VirtualFile virtualFile) {
		Exchange locked = lockedOutgoingQueue.get(virtualFile);

		if (locked == null) {
			for (VirtualFile vf : lockedOutgoingQueue.keySet()) {
				if (vf.getDatasetName().equals(virtualFile.getDatasetName())) {
					locked = lockedOutgoingQueue.get(vf);
				}
			}
		}

		return locked;
	}

	private void offerQueueToClient() {
		int size = outgoingQueue.size();
		boolean wasEmpty = size == 1;
		if (!hasIn && wasEmpty) {
			Timer providerTimer = new Timer();
			OdetteConfiguration configuration = endpoint.getConfiguration();
			providerTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						pollServer();
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeCamelException(e);
					}
				}
			}, configuration.getDelay());
		}
	}

	public boolean storeTempFile(File source, File file, Exchange exchange) {
		try {
			writeFileByFile(source, file);
			keepLastModified(exchange, file);
			return true;
		} catch (IOException e) {
			throw new GenericFileOperationFailedException("Cannot store file: " + file, e);
		}
	}

	private void writeFileByFile(File source, File target) throws IOException {
		FileChannel in = new FileInputStream(source).getChannel();
		FileChannel out = null;
		try {
			out = prepareOutputFileChannel(target, out);

			if (log.isTraceEnabled()) {
				log.trace("Using FileChannel to transfer from: " + in + " to: " + out);
			}

			long size = in.size();
			long position = 0;
			while (position < size) {
				position += in.transferTo(position, TEMP_COPY_BUFFER_SIZE, out);
			}
		} finally {
			IOHelper.close(in, source.getName(), log);
			IOHelper.close(out, source.getName(), log);
		}
	}

	private void keepLastModified(Exchange exchange, File file) {
		Long last;
		Date date = exchange.getIn().getHeader(Exchange.FILE_LAST_MODIFIED, Date.class);
		if (date != null) {
			last = date.getTime();
		} else {
			// fallback and try a long
			last = exchange.getIn().getHeader(Exchange.FILE_LAST_MODIFIED, Long.class);
		}
		if (last != null) {
			boolean result = file.setLastModified(last);
			if (log.isTraceEnabled()) {
				log.trace("Keeping last modified timestamp: " + last + " on file: " + file + " with result: " + result);
			}
		}
	}

	/**
	 * Creates and prepares the output file channel. Will position itself in
	 * correct position.
	 */
	private FileChannel prepareOutputFileChannel(File target, FileChannel out) throws IOException {
		out = new RandomAccessFile(target, "rw").getChannel();
		out = out.position(out.size());
		return out;
	}

	public void deleteTemporaryFileIfNeeded(VirtualFile virtualFile) {
		File file = virtualFile.getFile();
		String path = file.getPath();
		if (temporaryFiles.remove(path)) {
			deleteFile(path);
		}
	}

	public void notifyOfTemporaryFile(VirtualFile virtualFile) {
		temporaryFiles.add(virtualFile.getFile().getPath());
	}

	public boolean hasOutgoingObjects() {
		return !outgoingQueue.isEmpty();
	}

	public StartFileResponse acceptStartFile(VirtualFile incomingFile) {
		IncomingFileResponse incomingFileResponse = endpoint.askConsumerForIncomingFile(incomingFile);

		return incomingFileResponse.createStartFileResponse();
	}

	public void onReceiveFileStart(VirtualFile virtualFile, long answerCount) {
	}

	public EndFileResponse onReceiveFileEnd(VirtualFile virtualFile, long recordCount, long unitCount) {
		endpoint.notifyConsumerOf(virtualFile);

		// send the EERP back - request change direction (true)
		// only if there are objects on outgoing queue
		return DefaultEndFileResponse.positiveAnswer(hasOutgoingObjects());
	}

	public void onDataSent(VirtualFile virtualFile, long totalOctetsSent) {
		if (exchangeInTransit != null) {
			Message in = exchangeInTransit.getIn();
			Long hOctetsSent = in.getHeader(OdetteEndpoint.ODETTE_TOTAL_OCTETS_SENT, 0, Long.class);
			totalOctetsSent += hOctetsSent;
			in.setHeader(OdetteEndpoint.ODETTE_TOTAL_OCTETS_SENT, totalOctetsSent);
		}
	}

	public void onSendFileEnd(VirtualFile virtualFile) {
		deleteTemporaryFileIfNeeded(virtualFile);
		notifyExchangeInTransit(virtualFile);
	}

	public void onSendFileError(VirtualFile virtualFile, AnswerReasonInfo reason, boolean retryLater) {
		if (exchangeInTransit != null) {
			exchangeInTransit.getIn().setHeader(OdetteEndpoint.ODETTE_ANSWER_REASON, reason.getAnswerReason());
			exchangeInTransit.getIn().setHeader(OdetteEndpoint.ODETTE_REASON_TEXT, reason.getReasonText());
			exchangeInTransit.getIn().setFault(true);
		}

		notifyExchangeInTransit(virtualFile);
	}

	public void onNotificationReceived(DeliveryNotification notif) {
		endpoint.notifyConsumerOf(notif);
	}

	public OdetteFtpObject nextOftpObjectToSend() {
		return null;
	}

	public void onSendFileStart(VirtualFile virtualFile, long answerCount) {
		exchangeInTransit = findLockedExchange(virtualFile);

		Message in = exchangeInTransit.getIn();
		in.setHeader(OdetteEndpoint.ODETTE_ANSWER_COUNT, answerCount);
		in.setHeader(OdetteEndpoint.ODETTE_SEND_FILE_STARTED, Calendar.getInstance().getTime());

		OdetteConfiguration configuration = endpoint.getConfiguration();
		boolean defaultWaitForDelivery = configuration.isWaitForDelivery();
		boolean waitForDelivery = in.getHeader(OdetteEndpoint.ODETTE_WAIT_FOR_DELIVERY, defaultWaitForDelivery,
				Boolean.class);

		if (!waitForDelivery) {
			notifyExchangeInTransit(virtualFile);
		}
	}

	private void notifyExchangeInTransit(VirtualFile vf) {
		Exchange e = exchangeInTransit;
		exchangeInTransit = null;
		lockedOutgoingQueue.remove(vf);
		notifyLockedObject(e);
	}

	public void onNotificationSent(DeliveryNotification notif) {
	}

	public void onDataReceived(VirtualFile virtualFile, long totalOctetsReceived) {
	}

	public void onReceiveFileError(VirtualFile virtualFile, AnswerReasonInfo reason) {
	}

	public void onSessionStart() {
	}

	public void onSessionEnd() {
		// exchanges that sent file to this endpoint are still waiting for an
		// EERP but they weren't sent. Let's notify them and let them go
		notifyLockedObjects(lockedOutgoingQueue.values());
		lockedOutgoingQueue.clear();
	}

	public void onExceptionCaught(Throwable cause) {
	}

	public void destroy() {
		onSessionEnd();
	}

	public void init(OdetteFtpSession session) throws OdetteFtpException {
	}

	public boolean existsFile(String absoluteFilePath) {
		return new FileOperations().existsFile(absoluteFilePath);
	}

	public boolean deleteFile(String path) {
		return new FileOperations().deleteFile(path);
	}

	private void notifyLockedObject(Object locked) {
		if (locked != null) {
			synchronized (locked) {
				locked.notifyAll();
			}
		}
	}

	private void notifyLockedObjects(Collection<Exchange> collection) {
		for (Object locked : new ArrayList<Exchange>(collection)) {
			if (locked == null) {
				continue;
			}

			notifyLockedObject(locked);
		}
	}

}
