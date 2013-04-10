/**
 * The Accord Project, http://accordproject.org
 * Copyright (C) 2005-2013 Rafael Marins, http://rafaelmarins.com
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
 */
package org.neociclo.odetteftp.support;

import java.util.Queue;

import org.neociclo.odetteftp.oftplet.AnswerReasonInfo;
import org.neociclo.odetteftp.protocol.DefaultEndFileResponse;
import org.neociclo.odetteftp.oftplet.EndFileResponse;
import org.neociclo.odetteftp.oftplet.OftpletListener;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 */
public class SharedQueueOftpletListener implements OftpletListener {

    private Queue<OdetteFtpObject> incoming;
	private Queue<OdetteFtpObject> outgoing;
    private OftpletEventListener eventListener;

    public SharedQueueOftpletListener(Queue<OdetteFtpObject> incoming, Queue<OdetteFtpObject> outgoing) {
        super();
        this.incoming = incoming;
        this.outgoing = outgoing;
    }

    public StartFileResponse acceptStartFile(VirtualFile virtualFile) {

        if (eventListener != null) {
            return eventListener.acceptStartFile(virtualFile);
        }

        return null;
    }

    public void onReceiveFileStart(VirtualFile virtualFile, long answerCount) {
        if (eventListener != null) {
            eventListener.onReceiveFileStart(virtualFile, answerCount);
        }
    }

    public void onDataReceived(VirtualFile virtualFile, long totalOctetsReceived) {
        if (eventListener != null) {
            eventListener.onDataReceived(virtualFile, totalOctetsReceived);
        }
    }

    public EndFileResponse onReceiveFileEnd(VirtualFile virtualFile, long recordCount, long unitCount) {

    	if (incoming != null) {
    		incoming.add(virtualFile);
    	}

        if (eventListener != null) {
            EndFileResponse listenerResponse = eventListener.onReceiveFileEnd(virtualFile, recordCount, unitCount);
            if (listenerResponse != null) {
            	return listenerResponse;
            }
        }

        boolean changeDirection = false;
        if (outgoing != null) {
        	changeDirection = !outgoing.isEmpty();
        }

        return DefaultEndFileResponse.positiveEndFileAnswer(changeDirection);
    }

    public void onReceiveFileError(VirtualFile virtualFile, AnswerReasonInfo reason) {
        if (eventListener != null) {
            eventListener.onReceiveFileError(virtualFile, reason);
        }
    }

    public void onNotificationReceived(DeliveryNotification notif) {
    	if (incoming != null) {
    		incoming.offer(notif);
    	}
        if (eventListener != null) {
            eventListener.onNotificationReceived(notif);
        }
    }

    public void setEventListener(OftpletEventListener eventListener) {
        this.eventListener = eventListener;
    }

}
