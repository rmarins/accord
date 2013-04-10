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
