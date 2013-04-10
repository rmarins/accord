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
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpletListenerAdapter implements OftpletListener {

    protected Oftplet oftplet;

    public OftpletListenerAdapter() {
        super();
    }

    public OftpletListenerAdapter(Oftplet oftplet) {
        super();
        this.oftplet = oftplet;
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

}
