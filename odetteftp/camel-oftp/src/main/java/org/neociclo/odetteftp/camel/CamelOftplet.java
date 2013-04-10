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
package org.neociclo.odetteftp.camel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.security.auth.callback.CallbackHandler;

import org.apache.camel.RuntimeCamelException;
import org.neociclo.odetteftp.EntityType;
import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.oftplet.AnswerReasonInfo;
import org.neociclo.odetteftp.oftplet.EndFileResponse;
import org.neociclo.odetteftp.oftplet.Oftplet;
import org.neociclo.odetteftp.oftplet.OftpletListener;
import org.neociclo.odetteftp.oftplet.OftpletSpeaker;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.security.DefaultSecurityContext;
import org.neociclo.odetteftp.security.SecurityContext;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 */
public class CamelOftplet implements Oftplet, OftpletSpeaker, OftpletListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(CamelOftplet.class);

	private static Map<String, Observer> observerRegistry = new HashMap<String, Observer>();

	private OdetteFtpConfiguration config;
	private OftpOperations operations;
	private DefaultSecurityContext securityContext;

	private OdetteFtpSession session;
	private Queue<OdetteFtpObject> outgoingQueue = new ConcurrentLinkedQueue<OdetteFtpObject>();

	public CamelOftplet(OdetteFtpConfiguration defaultConfig, CallbackHandler callbackHandler,
			OftpOperations operations) {
		super();
		this.config = defaultConfig;
		this.securityContext = new DefaultSecurityContext(callbackHandler);
		this.operations = operations;
	}

	// Oftplet implementation
	// -------------------------------------------------------------------------

	public void init(OdetteFtpSession s) throws OdetteFtpException {
		this.session = s;
		config.setup(session);
	}

	public void destroy() {
		this.session = null;
		this.config = null;
		this.securityContext = null;
		this.operations = null;
	}

	public SecurityContext getSecurityContext() {
		return securityContext;
	}

	public void onSessionStart() {
		registerOutgoingExchangeObserver(session.getUserCode(), outgoingQueue);
		operations.onSessionStart(session);

		if (session.getEntityType() == EntityType.RESPONDER) {
			// retrieve user outgoing exchange and populate outgoing queue
			List<OdetteFtpObject> requests = operations.listUserOutgoingExchanges(session.getUserCode(), session);
			if (requests != null) {
				outgoingQueue.addAll(requests);
			}
		}
	}

	public void onSessionEnd() {
		operations.onSessionEnd(session);

		String userCode = session.getUserCode(); // equals null if not authenticated
		if (session.getEntityType() == EntityType.RESPONDER && userCode != null) {
			unregisterOutgoingExchangeObserver(session.getUserCode());
		}
	}

	public void onExceptionCaught(Throwable cause) {
		LOGGER.error("onExceptionCaught()", cause);
	}

	public boolean isProtocolVersionSupported(OdetteFtpVersion version) {
		// check if server accepts downgrading the version
		OftpSettings c = operations.getSettings();
		OdetteFtpVersion minVersion = c.getVersion();
		if (c.getDowngradeVersion() != null) {
			minVersion = c.getDowngradeVersion();
		}
		return version.isEqualOrOlder(minVersion);
	}

	public OftpletListener getListener() {
		return this;
	}

	public OftpletSpeaker getSpeaker() {
		return this;
	}

	// OftpletSpeaker implementation
	// -------------------------------------------------------------------------

	public OdetteFtpObject nextOftpObjectToSend() {
		LOGGER.trace("nextOftpObjectToSend() - begin - {}", outgoingQueue);
    	OdetteFtpObject next = null;
//    	// TODO implement some locking to notify the routes of onSessionStart
//    	// event and wait for initial outgoing OdetteFtpObject(s) to send
    	synchronized (outgoingQueue) {
	    	if (outgoingQueue != null) {
	    		next = outgoingQueue.poll();
	    		if (next == null) {
	    			try {
	    				// TODO sort out the 500 millis as a configurable endpoint parameter
	    				LOGGER.trace("nextOftpObjectToSend() - wait for request-reply in 0.5sec");
						outgoingQueue.wait(500L);
			    		next = outgoingQueue.poll();
					} catch (InterruptedException e) {
						throw new RuntimeCamelException(e);
					}
	    		}
	    	}
    	}
        return next;
	}

	public void onSendFileStart(VirtualFile vf, long answerCount) {
		// TODO Auto-generated method stub
		
	}

	public void onDataSent(VirtualFile vf, long totalOctetsSent) {
		// TODO Auto-generated method stub
		
	}

	public void onSendFileEnd(VirtualFile vf) {
		// TODO Auto-generated method stub
		
	}

	public void onSendFileError(VirtualFile vf, AnswerReasonInfo reason, boolean retryLater) {
		// TODO Auto-generated method stub
		
	}

	public void onNotificationSent(DeliveryNotification notif) {
		// TODO Auto-generated method stub
		
	}

	// OftpletSpeaker implementation
	// -------------------------------------------------------------------------

	public StartFileResponse acceptStartFile(VirtualFile vf) {
		return operations.acceptStartFile(session, vf);
	}

	public void onReceiveFileStart(VirtualFile vf, long answerCount) {
		// TODO Auto-generated method stub
		
	}

	public void onDataReceived(VirtualFile vf, long totalOctetsReceived) {
		// TODO Auto-generated method stub
		
	}

	public EndFileResponse onReceiveFileEnd(VirtualFile vf, long recordCount, long unitCount) {
		return operations.onReceiveFileEnd(session, vf, recordCount, unitCount);
	}

	public void onReceiveFileError(VirtualFile vf, AnswerReasonInfo reason) {
		// TODO Auto-generated method stub
		
	}

	public void onNotificationReceived(DeliveryNotification notif) {
		// TODO Auto-generated method stub
		
	}

	// Implementation specific methods
	// -------------------------------------------------------------------------

	public void registerOutgoingExchangeObserver(String userCode, Queue<OdetteFtpObject> outgoingQueue) {

		IOftpEndpoint endpoint = operations.getEndpoint();

		String oid = userCode.toLowerCase();
		Observer observer;

		boolean isServer = (session.getEntityType() == EntityType.RESPONDER); 
		observer = new ProducerOnProcessObserver(outgoingQueue, isServer, oid);
		endpoint.getOutgoingRequestsObservable().addObserver(observer);
		observerRegistry.put(oid, observer);

		LOGGER.trace("Observer registered: {}", observer);
	}

	public void unregisterOutgoingExchangeObserver(String userCode) {

		IOftpEndpoint endpoint = operations.getEndpoint();

		String oid = userCode.toLowerCase();
		Observer observer;

		observer = observerRegistry.get(oid);
		endpoint.getOutgoingRequestsObservable().deleteObserver(observer);
		observerRegistry.remove(observer);

		LOGGER.trace("Observer unregistered: {}", observer);
	}

}
