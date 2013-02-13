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
package org.neociclo.odetteftp.camel;

import java.util.EventObject;

import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpEvent extends EventObject {

	// Odette FTP message events
	public static final String OFTP_ON_SESSION_START_EVENT = "onSessionStart";
	public static final String OFTP_ON_SESSION_END_EVENT = "onSessionEnd";

	private static final long serialVersionUID = 1L;

	public static OftpEvent onSessionStartEvent(OdetteFtpSession session) {
		return new OftpEvent(OFTP_ON_SESSION_START_EVENT, session);
	}

	public static OftpEvent onSessionEndEvent(OdetteFtpSession session) {
		return new OftpEvent(OFTP_ON_SESSION_END_EVENT, session);
	}

	private String eventName;
	private OdetteFtpObject requestObject;

	public OftpEvent(OdetteFtpSession session) {
		this(null, session);
	}

	public OftpEvent(String eventName, OdetteFtpSession session) {
		this(eventName, null, session);
	}

	public OftpEvent(String eventName, OdetteFtpObject requestObject, OdetteFtpSession session) {
		super(session);
		setEventName(eventName);
		setRequestObject(requestObject);
	}

	@Override
	public OdetteFtpSession getSource() {
		return (OdetteFtpSession) super.getSource();
	}

	public OdetteFtpSession getSession() {
		return getSource();
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String name) {
		this.eventName = name;
	}

	public OdetteFtpObject getRequestObject() {
		return requestObject;
	}

	public void setRequestObject(OdetteFtpObject o) {
		this.requestObject = o;
	}

}
