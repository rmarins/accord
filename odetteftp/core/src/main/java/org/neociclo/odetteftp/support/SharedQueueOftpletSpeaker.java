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

import org.neociclo.odetteftp.oftplet.AnswerReasonInfo;
import org.neociclo.odetteftp.oftplet.OftpletSpeaker;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class SharedQueueOftpletSpeaker implements OftpletSpeaker {

    private Queue<OdetteFtpObject> outgoing;
    private Queue<OdetteFtpObject> outgoingDone;
    private OftpletEventListener eventListener;

    public SharedQueueOftpletSpeaker(Queue<OdetteFtpObject> outgoing, Queue<OdetteFtpObject> outgoingDone) {
        super();
        this.outgoing = outgoing;
        this.outgoingDone = outgoingDone;
    }

    public OdetteFtpObject nextOftpObjectToSend() {
    	OdetteFtpObject next = null;
    	if (outgoing != null) {
    		next = outgoing.poll();
    	}
        return next;
    }

    public void onSendFileStart(VirtualFile virtualFile, long answerCount) {
        if (eventListener != null) {
            eventListener.onSendFileStart(virtualFile, answerCount);
        }
    }

    public void onDataSent(VirtualFile virtualFile, long totalOctetsSent) {
        if (eventListener != null) {
            eventListener.onDataSent(virtualFile, totalOctetsSent);
        }
    }

    public void onSendFileEnd(VirtualFile virtualFile) {
    	if (outgoingDone != null) {
    		outgoingDone.offer(virtualFile);
    	}
        if (eventListener != null) {
            eventListener.onSendFileEnd(virtualFile);
        }
    }

    public void onSendFileError(VirtualFile virtualFile, AnswerReasonInfo reason, boolean retryLater) {
        if (eventListener != null) {
            eventListener.onSendFileError(virtualFile, reason, retryLater);
        }
    }

    public void onNotificationSent(DeliveryNotification notif) {
    	if (outgoingDone != null) {
    		outgoingDone.offer(notif);
    	}
        if (eventListener != null) {
            eventListener.onNotificationSent(notif);
        }
    }

	public void setEventListener(OftpletEventListener listener) {
		this.eventListener = listener;
	}

}
