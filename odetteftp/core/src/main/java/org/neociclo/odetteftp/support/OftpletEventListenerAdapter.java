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

	public void configure(OdetteFtpSession session) {
	}

}
