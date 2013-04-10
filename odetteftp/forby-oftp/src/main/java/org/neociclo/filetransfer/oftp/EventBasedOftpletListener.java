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
package org.neociclo.filetransfer.oftp;

import static org.neociclo.filetransfer.oftp.util.OftpConstants.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.neociclo.accord.filetransfer.DefaultIncomingFileTransfer;
import org.neociclo.accord.filetransfer.IFileRangeSpecification;
import org.neociclo.accord.filetransfer.IFileTransferListener;
import org.neociclo.accord.filetransfer.IIncomingRequestListener;
import org.neociclo.accord.filetransfer.events.DefaultFileTransferRequestEvent;
import org.neociclo.accord.filetransfer.events.FileTransferEvents;
import org.neociclo.accord.filetransfer.events.IFileTransferRequestEvent;
import org.neociclo.accord.filetransfer.events.IIncomingDeliveryAckEvent;
import org.neociclo.accord.filetransfer.events.IIncomingFileTransferReceiveDataEvent;
import org.neociclo.accord.filetransfer.events.IIncomingFileTransferReceiveDoneEvent;
import org.neociclo.accord.filetransfer.events.IIncomingFileTransferReceiveResumedEvent;
import org.neociclo.accord.filetransfer.events.IIncomingFileTransferReceiveStartEvent;
import org.neociclo.accord.filetransfer.spi.IRetrieveRequest;
import org.neociclo.odetteftp.protocol.AnswerReason;
import org.neociclo.odetteftp.protocol.DefaultStartFileResponse;
import org.neociclo.odetteftp.protocol.IDeliveryNotification;
import org.neociclo.odetteftp.protocol.IStartFileResponse;
import org.neociclo.odetteftp.protocol.IVirtualFile;
import org.neociclo.odetteftp.support.AnswerReasonInfo;
import org.neociclo.odetteftp.support.IOftpletListener;
import org.neociclo.odetteftp.util.ProtocolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
class EventBasedOftpletListener implements IOftpletListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventBasedOftpletListener.class);

    private Queue<IRetrieveRequest> retrieveQueue;
    private List<IIncomingRequestListener> incomingRequestListeners;

    private DefaultFileTransferRequestEvent incomingFileAcceptedEvent;

    /**
     * @param retrieveQueue
     * @param incomingRequestListeners
     */
    public EventBasedOftpletListener(Queue<IRetrieveRequest> retrieveQueue,
            List<IIncomingRequestListener> incomingRequestListeners) {
        super();
        this.retrieveQueue = retrieveQueue;
        this.incomingRequestListeners = incomingRequestListeners;
    }

    public IStartFileResponse acceptStartFile(IVirtualFile virtualFile) {

        if (retrieveQueue == null || retrieveQueue.isEmpty()) {
            return new DefaultStartFileResponse(false, AnswerReason.UNSPECIFIED,
                    "No retrieve request on this session.", true);
        }

        OftpFileTransferInfo fileTransferInfo = new OftpFileTransferInfo(virtualFile);

        // match the incoming start file/retrieve file transfer request event
        // against the retrieve requests enqueued until accepted
        Iterator<IRetrieveRequest> it = retrieveQueue.iterator();
        while (it.hasNext()) {

            IRetrieveRequest rr = it.next();
            IFileTransferRequestEvent requestEvent = FileTransferEvents.fileTransferRequestEvent(
                    rr.getRetrieveFileID(), fileTransferInfo, rr.getFileTransferListener());

            for (IIncomingRequestListener requestListener : incomingRequestListeners) {
                requestListener.handleFileTransferRequest(requestEvent);
                if (requestEvent.requestAccepted()) {
                    LOGGER.debug("File Transfer request event has been accepted: {}", requestEvent);
                    incomingFileAcceptedEvent = (DefaultFileTransferRequestEvent) requestEvent;
                    break;
                } else {
                    LOGGER.debug("File Transfer request event has been rejected: {}", requestListener);
                }
            }

            if (incomingFileAcceptedEvent != null) {
                break;
            }

        }

        // non-matched events is considered as rejected (unspecified)
        if (incomingFileAcceptedEvent == null) {
            DefaultStartFileResponse rejectedResponse = new DefaultStartFileResponse(false, AnswerReason.UNSPECIFIED,
                    "Start File rejected by user (API).", true);
            return rejectedResponse;
        }

        // start file request accepted
        DefaultIncomingFileTransfer incomingFile = incomingFileAcceptedEvent.getIncomingFileTransfer();
        incomingFile.setFileLength(ProtocolUtil.computeFileSizeInOctets(virtualFile.getSize(), virtualFile
                .getRecordFormat(), virtualFile.getRecordSize()));
        long offset = 0;
        IFileRangeSpecification range = incomingFile.getFileRangeSpecification();

        if (range != null) {
            ProtocolUtil.computeVirtualFileOffset(range.getStartPosition(), virtualFile.getRecordFormat(), virtualFile
                    .getRecordSize());
        }
        DefaultStartFileResponse acceptedStartFile = new DefaultStartFileResponse(true, offset);

        // set incoming file (local system) on the Virtual File
        virtualFile.setFile(incomingFile.getFile());

        return acceptedStartFile;
    }

    public void onReceiveFileStart(IVirtualFile virtualFile, long answerCount) {

        IFileTransferListener listener = incomingFileAcceptedEvent.getIncomingFileTransfer().getListener();
        if (listener == null) {
            return;
        }

        Map<String, String> responseHeaders = new HashMap<String, String>();
        responseHeaders.put(RESTART_OFFSET_RESPONSE_HEADER, Long.toString(answerCount));

        String fileID = incomingFileAcceptedEvent.getRequesterID();
        DefaultIncomingFileTransfer incomingFile = incomingFileAcceptedEvent.getIncomingFileTransfer();

        if (answerCount > 0) {

            incomingFile.setBytesReceived(ProtocolUtil.computeOffsetFilePosition(answerCount, virtualFile
                    .getRecordFormat(), virtualFile.getRecordSize()));
            incomingFile.setPercentComplete(answerCount / virtualFile.getSize());

            IIncomingFileTransferReceiveResumedEvent receiveResumedEvent = FileTransferEvents.receiveResumedEvent(
                    incomingFile, fileID, responseHeaders);
            listener.handleTransferEvent(receiveResumedEvent);
            
        } else {

            IIncomingFileTransferReceiveStartEvent receiveStartEvent = FileTransferEvents.receiveStartEvent(
                    incomingFile, fileID, responseHeaders);

            listener.handleTransferEvent(receiveStartEvent);
        }

    }

    public void onDataReceived(IVirtualFile virtualFile, long totalOctetsReceived) {

        IFileTransferListener listener = incomingFileAcceptedEvent.getIncomingFileTransfer().getListener();
        if (listener == null) {
            return;
        }

        DefaultIncomingFileTransfer incomingFile = incomingFileAcceptedEvent.getIncomingFileTransfer();

        incomingFile.setBytesReceived(totalOctetsReceived);
        incomingFile.setPercentComplete(totalOctetsReceived / incomingFile.getFileLength());

        IIncomingFileTransferReceiveDataEvent receiveDataEvent = FileTransferEvents.receiveDataEvent(incomingFile);

        listener.handleTransferEvent(receiveDataEvent);

    }

    public boolean onReceiveFileEnd(IVirtualFile virtualFile, long recordCount, long unitCount) {

        boolean changeDirection = false;

        IFileTransferListener listener = incomingFileAcceptedEvent.getIncomingFileTransfer().getListener();
        if (listener == null) {
            return changeDirection;
        }

        DefaultIncomingFileTransfer incomingFile = incomingFileAcceptedEvent.getIncomingFileTransfer();
        incomingFile.setPercentComplete(1.0);
        incomingFile.setDone(true);

        IIncomingFileTransferReceiveDoneEvent receiveDoneEvent = FileTransferEvents.receiveDoneEvent(incomingFile);
        listener.handleTransferEvent(receiveDoneEvent);

        return changeDirection;
    }

    public void onReceiveFileError(IVirtualFile virtualFile, AnswerReasonInfo reason) {

        IFileTransferListener listener = incomingFileAcceptedEvent.getIncomingFileTransfer().getListener();
        if (listener == null) {
            return;
        }

        DefaultIncomingFileTransfer incomingFile = incomingFileAcceptedEvent.getIncomingFileTransfer();
        incomingFile.setDone(true);

        IIncomingFileTransferReceiveDoneEvent receiveFailedEvent = FileTransferEvents.receiveDoneEvent(incomingFile,
                new Exception("Answer Reason: " + reason));
        listener.handleTransferEvent(receiveFailedEvent);

    }

    public void onNotificationReceived(IDeliveryNotification notif) {

        if (incomingRequestListeners == null || incomingRequestListeners.isEmpty()) {
            return;
        }

        OftpDeliveryAckInfo deliveryAckInfo = new OftpDeliveryAckInfo(notif);
        IIncomingDeliveryAckEvent deliveryAckEvent = FileTransferEvents.receivedDeliveryAckEvent(deliveryAckInfo);

        for (IIncomingRequestListener requestListener : incomingRequestListeners) {
            requestListener.handleFileTransferRequest(deliveryAckEvent);
        }

    }

}
