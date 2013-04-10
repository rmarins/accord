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
package org.neociclo.odetteftp.support;

import java.util.Queue;

import org.neociclo.odetteftp.oftplet.Oftplet;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.security.MappedCallbackHandler;

/**
 * @author Rafael Marins
 */
public class InOutSharedQueueOftpletFactory implements OftpletFactory {

	private OdetteFtpConfiguration config;
	private Queue<OdetteFtpObject> outgoing;
	private Queue<OdetteFtpObject> outgoingDone;
	private Queue<OdetteFtpObject> incoming;
	private OftpletEventListener eventListener;
	private MappedCallbackHandler callbackHandler;

	/**
	 * Constructor for this shared queue Oftplet factory implementation,
	 * supporting incoming and outgoing operations. File receiving depends on
	 * the InOutEventListener to be accepted (saved in local system).
	 * 
	 * If the incoming or outgoing constructor's arguments are not specified the
	 * OftpletListener and OftpletSpeaker are respectively will not be provided
	 * by the created Oftplet implementation.
	 * 
	 * @param config
	 * @param outgoing
	 *            queue of Odette FTP objects, Virtual File and Delivery
	 *            Notifications, for transmission.
	 * @param outgoingDone
	 *            where outgoing Odette FTP objects are pulled to after the
	 *            protocol send is acknowledged from the outgoing queue. For
	 *            instance, when a file is completely transmitted with an EFPA
	 *            or when the EERP/NERP is sent and returned a RTR command.
	 * @param incoming
	 *            where incoming Odette FTP objects, received Virtual Files and
	 *            Delivery Notifications, are kept.
	 */
	public InOutSharedQueueOftpletFactory(OdetteFtpConfiguration config, MappedCallbackHandler callbackHandler,
			Queue<OdetteFtpObject> outgoing, Queue<OdetteFtpObject> outgoingDone, Queue<OdetteFtpObject> incoming) {
		super();
		this.config = config;
		this.callbackHandler = callbackHandler;
		this.outgoing = outgoing;
		this.outgoingDone = outgoingDone;
		this.incoming = incoming;
	}

	public Oftplet createProvider() {
		InOutSharedQueueOftplet oftplet = new InOutSharedQueueOftplet(config, callbackHandler, outgoing, outgoingDone, incoming);
		if (eventListener != null) {
			oftplet.setEventListener(eventListener);
		}
		return oftplet;
	}

	public void setEventListener(OftpletEventListener eventListener) {
		this.eventListener = eventListener;
	}

	public OftpletEventListener getEventListener() {
		return eventListener;
	}

}
