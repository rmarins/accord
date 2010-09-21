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
package org.neociclo.odetteftp.examples.server;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.examples.support.DefaultSecurityContext;
import org.neociclo.odetteftp.examples.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.oftplet.AnswerReasonInfo;
import org.neociclo.odetteftp.oftplet.Oftplet;
import org.neociclo.odetteftp.oftplet.OftpletAdapter;
import org.neociclo.odetteftp.oftplet.OftpletListener;
import org.neociclo.odetteftp.oftplet.OftpletSpeaker;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.security.SecurityContext;
import org.neociclo.odetteftp.support.OftpletEventListener;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
class ServerOftplet extends OftpletAdapter implements Oftplet, OftpletSpeaker, OftpletListener {

	private OdetteFtpConfiguration config;
	private OftpletEventListener listener;
	private SecurityContext securityContext;

	public ServerOftplet(OdetteFtpConfiguration config, OftpletEventListener listener) {
		super();
		this.config = config;
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
		if (securityContext == null) {
			securityContext = new DefaultSecurityContext(config.getCallbackHandler());
		}
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

	public boolean onReceiveFileEnd(VirtualFile virtualFile, long recordCount, long unitCount) {
		return false;
	}

	public void onReceiveFileError(VirtualFile virtualFile, AnswerReasonInfo reason) {
	}

	public void onNotificationReceived(DeliveryNotification notif) {
	}

}
