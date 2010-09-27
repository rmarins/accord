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

import org.neociclo.odetteftp.protocol.AnswerReason;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public interface OftpletListener {

    /**
     * Callback method indicating a receive file request, used to construct
     * and return the VirtualFile instance. Returning <code>null</code>
     * corresponds to refuse the file receiving - reply with Start File Negative
     * Answer (SFNA) with {@link AnswerReason#UNSPECIFIED} reason.
     * <p/>
     * The given parameters provide required information to map the transferring
     * VirtualFile into the <i>Oftplet</i> implementation's local system.
     * 
     * @param virtualFile
     *            holder of file receive indication parameters.
     * @return the VirtualFile instance ready to start the file receiving.
     */
    StartFileResponse acceptStartFile(VirtualFile virtualFile);

    void onReceiveFileStart(VirtualFile virtualFile, long answerCount);

    void onDataReceived(VirtualFile virtualFile, long totalOctetsReceived);
    
    /**
     * Callback method indicating the file receive end request with given
     * parameters information used to check the integrity of the received file.
     * When an inconsistency is found this method should throw a
     * FileTransferException with the proper reason code.
     * <p/>
     * The returning <code>boolean</code> value indicates whether the other
     * peer, operating in the <i>Speaker</i> state, should issue a Change
     * Direction (CD) command and revert this <i>Oftplet</i> implementation
     * ODETTE-FTP entity state.
     * 
     * @param recordCount
     *            number of records the other ODETTE-FTP peer sent in the
     *            VirtualFile.
     * @param unitCount
     *            exact number of units (octets) transmitted.
     * @return whether to change direction after complete the file receiving.
     */
    boolean onReceiveFileEnd(VirtualFile virtualFile, long recordCount, long unitCount);
    
    void onReceiveFileError(VirtualFile virtualFile, AnswerReasonInfo reason);

    /**
     * Callback method to indicate the <i>Oftplet</i> about the receive of a
     * delivery notification. Called when processing the receive of the
     * End-to-End Response (EERP) or Negative End Response (NERP) command while
     * in the <i>Listener</i> state.
     * 
     * @param notif
     *            holder of the received delivery notification parameters.
     */
    void onNotificationReceived(DeliveryNotification notif);


}
