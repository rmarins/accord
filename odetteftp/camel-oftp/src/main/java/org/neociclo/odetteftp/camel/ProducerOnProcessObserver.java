/**
 * Neociclo Accord, Open Source B2B Integration Suite
 * Copyright (C) 2005-2010 Neociclo, http://www.neociclo.com
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
 *
 * $Id$
 */
package org.neociclo.odetteftp.camel;

import static org.neociclo.odetteftp.camel.OftpMessage.*;

import java.util.Observable;
import java.util.Observer;
import java.util.Queue;

import org.apache.camel.Exchange;
import org.apache.camel.ExpectedBodyTypeException;
import org.apache.camel.Message;
import org.apache.camel.NoSuchHeaderException;
import org.apache.camel.ValidationException;
import org.apache.camel.util.ObjectHelper;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds the received Odette FTP request to the corresponding producer's
 * outgoingQueue when matching the given local user code with the connecting
 * user OID from the Oftplet session.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class ProducerOnProcessObserver implements Observer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProducerOnProcessObserver.class);

	private boolean isServer = false;

	private String checkUserCode;
	private Queue<OdetteFtpObject> outgoingQueue;

	public ProducerOnProcessObserver(Queue<OdetteFtpObject> outgoingQueue, boolean isServer) {
		this(outgoingQueue, isServer, null);
	}

	public ProducerOnProcessObserver(Queue<OdetteFtpObject> outgoingQueue, boolean isServer, String checkRemoteUserCode) {
		super();
		ObjectHelper.notNull(outgoingQueue, "outgoingQueue");
		this.checkUserCode = checkRemoteUserCode;
		this.outgoingQueue = outgoingQueue;
		this.isServer = isServer;
	}

	public void update(Observable subject, Object arg) {

		Exchange exchange = (Exchange) arg;
		Message m = exchange.getIn();

		String remoteUser = m.getHeader(OFTP_SESSION_REMOTE_USER, String.class);;
		// when message doesn't has the OFTP user code configured in
		if (isServer && remoteUser == null) {
			LOGGER.trace("Bad exchange. Unknown user code: {}", exchange);
			exchange.setException(new NoSuchHeaderException(exchange, OFTP_SESSION_REMOTE_USER, String.class));
			return;
		}
		// user code doesn't match with of this observer
		else if (isServer && !checkUserCode.equalsIgnoreCase(remoteUser)) {
			LOGGER.trace("Skipping the ProducerOnProcessObserver for exchange: {}. UserCode doesn't match with "
					+ "[{}={}]", new Object[] { exchange, remoteUser, OFTP_SESSION_REMOTE_USER });
			exchange.setException(new ValidationException(exchange, OFTP_SESSION_REMOTE_USER + "=" + remoteUser
					+ " doesn't match with: " + checkUserCode));
			return;
		}

		OdetteFtpObject oftpOutgoingRequest = m.getBody(OdetteFtpObject.class);
		if (oftpOutgoingRequest == null) {
			LOGGER.warn("Cannot extract the received Odette FTP outgoing request from: {}",
					exchange);
			exchange.setException(new ExpectedBodyTypeException(exchange, OdetteFtpObject.class));
			return;
		}

		synchronized (outgoingQueue) {
			// add the received OFTP request to the Oftplet outgoing queue
			LOGGER.debug("ProducerOnProcessObserver updated with: {}. Adding the " +
					"Odette FTP outgoing request to the Queue: {}", arg, oftpOutgoingRequest);
			outgoingQueue.offer(oftpOutgoingRequest);
			outgoingQueue.notify();
		}

	}
}
