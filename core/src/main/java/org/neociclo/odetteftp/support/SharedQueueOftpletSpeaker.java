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
    private InOutOftpletEventListener eventListener;

    public SharedQueueOftpletSpeaker(Queue<OdetteFtpObject> outgoing, Queue<OdetteFtpObject> outgoingDone) {
        super();
        this.outgoing = outgoing;
        this.outgoingDone = outgoingDone;
    }

    public OdetteFtpObject nextOftpObjectToSend() {
        return outgoing.poll();
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

    public void setEventListenet(InOutOftpletEventListener eventListener) {
        this.eventListener = eventListener;
    }

}
