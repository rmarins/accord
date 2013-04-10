/**
 * Neociclo Accord, Open Source B2Bi Middleware
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
package org.neociclo.odetteftp.support;

import java.util.Queue;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.oftplet.Oftplet;
import org.neociclo.odetteftp.oftplet.OftpletAdapter;
import org.neociclo.odetteftp.oftplet.OftpletListener;
import org.neociclo.odetteftp.oftplet.OftpletSpeaker;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.security.DefaultSecurityContext;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.SecurityContext;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class InOutSharedQueueOftplet extends OftpletAdapter implements Oftplet {

	private OdetteFtpConfiguration config;
	private SecurityContext securityContext;
	private SharedQueueOftpletListener listener;
	private SharedQueueOftpletSpeaker speaker;
	private OftpletEventListener wrappedListener;

	public InOutSharedQueueOftplet(OdetteFtpConfiguration config, MappedCallbackHandler callbackHandler, Queue<OdetteFtpObject> outgoing,
			Queue<OdetteFtpObject> outgoingDone, Queue<OdetteFtpObject> incoming) {

		super();
		this.config = config;
		this.securityContext = new DefaultSecurityContext(callbackHandler);

		this.listener = new SharedQueueOftpletListener(incoming, outgoing);
		this.speaker = new SharedQueueOftpletSpeaker(outgoing, outgoingDone);
	}

	@Override
	public SecurityContext getSecurityContext() {
		return securityContext;
	}

	@Override
	public void init(OdetteFtpSession s) throws OdetteFtpException {
		config.setup(s);
	}

	@Override
	public boolean isProtocolVersionSupported(OdetteFtpVersion version) {
		if (config.getVersion() != null) {
			return config.getVersion().equals(version);
		} else {
			return super.isProtocolVersionSupported(version);
		}
	}

	@Override
	public OftpletListener getListener() {
		return listener;
	}

	@Override
	public OftpletSpeaker getSpeaker() {
		return speaker;
	}

	public void setEventListener(OftpletEventListener eventListener) {
		if (speaker != null) {
			speaker.setEventListener(eventListener);
		}
		if (listener != null) {
			listener.setEventListener(eventListener);
		}
		this.wrappedListener = eventListener;
	}

	@Override
	public void onSessionStart() {
		if (wrappedListener != null) {
			wrappedListener.onSessionStart();
		}
	}

	@Override
	public void onSessionEnd() {
		if (wrappedListener != null) {
			wrappedListener.onSessionEnd();
		}
	}

	@Override
	public void onExceptionCaught(Throwable cause) {
		if (wrappedListener != null) {
			wrappedListener.onExceptionCaught(cause);
		}
	}

}
