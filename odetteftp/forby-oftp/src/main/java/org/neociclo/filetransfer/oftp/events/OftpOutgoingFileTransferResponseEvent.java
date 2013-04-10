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
package org.neociclo.filetransfer.oftp.events;

import org.neociclo.accord.filetransfer.IOutgoingFileTransfer;
import org.neociclo.accord.filetransfer.events.OutgoingFileTransferResponseEvent;
import org.neociclo.odetteftp.protocol.AnswerReason;

/**
 * @author Rafael Marins
 */
public class OftpOutgoingFileTransferResponseEvent extends OutgoingFileTransferResponseEvent implements IOftpOutgoingFileTransferResponseEvent {

    protected AnswerReason answerReason;

    protected boolean rejectResponseLocal;

    public OftpOutgoingFileTransferResponseEvent(IOutgoingFileTransfer source, boolean requestAccepted,
            AnswerReason reason) {
        this(source, requestAccepted, reason, false);
    }

    public OftpOutgoingFileTransferResponseEvent(IOutgoingFileTransfer source, boolean requestAccepted,
            AnswerReason reason, boolean rejectResponseLocal) {
        super(source, requestAccepted);
        this.answerReason = reason;
        this.rejectResponseLocal = rejectResponseLocal;
    }

    /* (non-Javadoc)
     * @see org.neociclo.filetransfer.oftp.events.IOftpOutgoingFileTransferResponseEvent#getAnswerReason()
     */
    public AnswerReason getAnswerReason() {
        return answerReason;
    }

    /* (non-Javadoc)
     * @see org.neociclo.filetransfer.oftp.events.IOftpOutgoingFileTransferResponseEvent#isRejectResponseLocal()
     */
    public boolean isRejectResponseLocal() {
        return rejectResponseLocal;
    }
}
