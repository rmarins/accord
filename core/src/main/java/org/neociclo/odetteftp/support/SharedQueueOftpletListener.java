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
import org.neociclo.odetteftp.oftplet.EndFileResponse;
import org.neociclo.odetteftp.oftplet.OftpletListener;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DefaultEndFileResponse;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class SharedQueueOftpletListener implements OftpletListener {

    private Queue<OdetteFtpObject> incoming;
    private OftpletEventListener eventListener;

    public SharedQueueOftpletListener(Queue<OdetteFtpObject> incoming) {
        super();
        this.incoming = incoming;
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
            return eventListener.onReceiveFileEnd(virtualFile, recordCount, unitCount);
        }
        return DefaultEndFileResponse.positiveEndFileAnswer();
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
