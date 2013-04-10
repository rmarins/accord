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
