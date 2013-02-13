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
package org.neociclo.filetransfer.oftp.events;

import org.neociclo.accord.filetransfer.IOutgoingFileTransfer;
import org.neociclo.accord.filetransfer.events.OutgoingFileTransferResponseEvent;
import org.neociclo.odetteftp.protocol.AnswerReason;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
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
