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

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.oftplet.AnswerReasonInfo;
import org.neociclo.odetteftp.oftplet.EndFileResponse;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpletEventListenerAdapter implements OftpletEventListener {

    public OdetteFtpObject nextOftpObjectToSend() {
        return null;
    }

    public void onDataSent(VirtualFile virtualFile, long totalOctetsSent) {
    }

    public void onNotificationSent(DeliveryNotification notif) {
    }

    public void onSendFileEnd(VirtualFile virtualFile) {
    }

    public void onSendFileError(VirtualFile virtualFile, AnswerReasonInfo reason, boolean retryLater) {
    }

    public void onSendFileStart(VirtualFile virtualFile, long answerCount) {
    }

    public StartFileResponse acceptStartFile(VirtualFile virtualFile) {
        return null;
    }

    public void onDataReceived(VirtualFile virtualFile, long totalOctetsReceived) {
    }

    public void onNotificationReceived(DeliveryNotification notif) {
    }

    public EndFileResponse onReceiveFileEnd(VirtualFile virtualFile, long recordCount, long unitCount) {
        return null;
    }

    public void onReceiveFileError(VirtualFile virtualFile, AnswerReasonInfo reason) {
    }

    public void onReceiveFileStart(VirtualFile virtualFile, long answerCount) {
    }

	public void onExceptionCaught(Throwable cause) {
	}

	public void onSessionStart() {
	}

	public void onSessionEnd() {
	}

	public void destroy() {
	}

	public void init(OdetteFtpSession session) throws OdetteFtpException {
	}

}
