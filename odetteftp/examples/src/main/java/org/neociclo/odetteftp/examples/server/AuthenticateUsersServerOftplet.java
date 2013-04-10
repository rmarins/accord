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
package org.neociclo.odetteftp.examples.server;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.oftplet.AnswerReasonInfo;
import org.neociclo.odetteftp.oftplet.EndFileResponse;
import org.neociclo.odetteftp.oftplet.Oftplet;
import org.neociclo.odetteftp.oftplet.OftpletAdapter;
import org.neociclo.odetteftp.oftplet.OftpletListener;
import org.neociclo.odetteftp.oftplet.OftpletSpeaker;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.security.DefaultSecurityContext;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.SecurityContext;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.support.OftpletEventListener;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
class AuthenticateUsersServerOftplet extends OftpletAdapter implements Oftplet, OftpletSpeaker, OftpletListener {

	private OdetteFtpConfiguration config;
	private OftpletEventListener listener;
	private SecurityContext securityContext;

	public AuthenticateUsersServerOftplet(OdetteFtpConfiguration config, MappedCallbackHandler securityCallbackHandler, OftpletEventListener listener) {
		super();
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
		config.setup(session);
		if (listener != null) {
			listener.init(session);
		}
	}

	@Override
	public void destroy() {
		this.config = null;
		if (listener != null) {
			listener.destroy();
		}
	}

	@Override
	public void onSessionStart() {
		if (listener != null) {
			listener.onSessionStart();
		}
	}

	@Override
	public void onExceptionCaught(Throwable cause) {
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
		return null;
	}

	public void onSendFileStart(VirtualFile virtualFile, long answerCount) {
	}

	public void onDataSent(VirtualFile virtualFile, long totalOctetsSent) {
	}

	public void onSendFileEnd(VirtualFile virtualFile) {
	}

	public void onSendFileError(VirtualFile virtualFile, AnswerReasonInfo reason, boolean retryLater) {
	}

	public void onNotificationSent(DeliveryNotification notif) {
	}

	// -------------------------------------------------------------------------
	//   OftpletListener implementation
	// -------------------------------------------------------------------------

	public StartFileResponse acceptStartFile(VirtualFile virtualFile) {
		return null;
	}

	public void onReceiveFileStart(VirtualFile virtualFile, long answerCount) {
	}

	public void onDataReceived(VirtualFile virtualFile, long totalOctetsReceived) {
	}

	public EndFileResponse onReceiveFileEnd(VirtualFile virtualFile, long recordCount, long unitCount) {
		return null;
	}

	public void onReceiveFileError(VirtualFile virtualFile, AnswerReasonInfo reason) {
	}

	public void onNotificationReceived(DeliveryNotification notif) {
	}

}
