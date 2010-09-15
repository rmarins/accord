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
package org.neociclo.odetteftp.support;

import java.util.Queue;

import org.neociclo.odetteftp.oftplet.Oftplet;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class InOutSharedQueueOftpletFactory implements OftpletFactory {

	private SessionConfig sessionConfig;
	private Queue<OdetteFtpObject> outgoing;
	private Queue<OdetteFtpObject> outgoingDone;
	private Queue<OdetteFtpObject> incoming;
	private InOutOftpletEventListener eventListener;

	/**
	 * Constructor for this shared queue Oftplet factory implementation,
	 * supporting incoming and outgoing operations. File receiving depends on
	 * the InOutEventListener to be accepted (saved in local system).
	 * 
	 * If the incoming or outgoing constructor's arguments are not specified the
	 * OftpletListener and OftpletSpeaker are respectively will not be provided
	 * by the created Oftplet implementation.
	 * 
	 * @param sessionConfig
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
	public InOutSharedQueueOftpletFactory(SessionConfig sessionConfig, Queue<OdetteFtpObject> outgoing,
			Queue<OdetteFtpObject> outgoingDone, Queue<OdetteFtpObject> incoming) {
		super();
		this.sessionConfig = sessionConfig;
		this.outgoing = outgoing;
		this.outgoingDone = outgoingDone;
		this.incoming = incoming;
	}

	public Oftplet createProvider() {
		InOutSharedQueueOftplet oftplet = new InOutSharedQueueOftplet(sessionConfig, outgoing, outgoingDone, incoming);
		if (eventListener != null) {
			oftplet.setEventListener(eventListener);
		}
		return oftplet;
	}

	public void setEventListener(InOutOftpletEventListener eventListener) {
		this.eventListener = eventListener;
	}

	public InOutOftpletEventListener getEventListener() {
		return eventListener;
	}

}
