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
package org.neociclo.odetteftp.oftplet;

import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public interface OftpletSpeaker {

    OdetteFtpObject nextOftpObjectToSend();
    
    /**
     * Callback method to indicate the given file transfer will begin. Called
     * after the receive of Start File Positive Answer (SFPA) command while in
     * the <i>Speaker</i> state, just before the first DATA command is released.
     * 
     * @param virtualFile
     *            the bundle of transfer info and virtual file being
     *            transferred.
     * @param answerCount negotiated restart offset
     */
    void onSendFileStart(VirtualFile virtualFile, long answerCount);

    void onDataSent(VirtualFile virtualFile, long totalOctetsSent);

    /**
     * Callback method invoked when the file is fully transmitted. It happens
     * after receiving the End File Positive Answer (EFNA) command while in the
     * <i>Speaker</i> state.
     * @param virtualFile 
     */
    void onSendFileEnd(VirtualFile virtualFile);

    /**
     * Callback method to indicate the transmitting file is not accepted by the
     * other peer - being in the Speaker state. It is always the case when
     * receiving a Start File Negative Answer (SFNA) and End File Negative
     * Answer (EFNA) protocol commands.
     * <p/>
     * Any other kind of error, such as IoException or FileTransferException, it
     * should be handled by the {@link Oftplet#onExceptionCaught(Throwable)}
     * method.
     * @param virtualFile 
     * 
     * @param reason
     * @param reasonText
     * @param retryLater
     */
    void onSendFileError(VirtualFile virtualFile, AnswerReasonInfo reason, boolean retryLater);
    
    void onNotificationSent(DeliveryNotification notif);

}
