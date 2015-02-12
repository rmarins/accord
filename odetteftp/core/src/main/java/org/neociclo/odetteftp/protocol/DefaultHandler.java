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
package org.neociclo.odetteftp.protocol;

import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V14;
import static org.neociclo.odetteftp.protocol.AnswerReason.ACCESS_METHOD_FAILURE;
import static org.neociclo.odetteftp.protocol.AnswerReason.INVALID_BYTE_COUNT;
import static org.neociclo.odetteftp.protocol.AnswerReason.INVALID_RECORD_COUNT;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EFIDRCNT_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EFIDUCNT_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EFPACD_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDDSN_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFNARRTR_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFPAACNT_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SSIDCMPR_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SSIDCODE_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SSIDCRED_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SSIDLEV_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SSIDPSWD_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SSIDREST_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SSIDSDEB_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SSIDSPEC_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SSIDSR_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SSIDUSER_FIELD;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.MAX_OEB_LENGTH;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.MIN_OEB_LENGTH;
import static org.neociclo.odetteftp.util.ProtocolUtil.computeVirtualFileRecordCount;
import static org.neociclo.odetteftp.util.ProtocolUtil.valueOfYesNo;
import static org.neociclo.odetteftp.util.SessionHelper.getSessionCurrentRequest;
import static org.neociclo.odetteftp.util.SessionHelper.getSessionFileChannel;
import static org.neociclo.odetteftp.util.SessionHelper.getSessionOftplet;
import static org.neociclo.odetteftp.util.SessionHelper.getSessionOutgoingDataExchangeBuffer;
import static org.neociclo.odetteftp.util.SessionHelper.isInitiator;
import static org.neociclo.odetteftp.util.SessionHelper.isReceivingSupported;
import static org.neociclo.odetteftp.util.SessionHelper.isSendingSupported;
import static org.neociclo.odetteftp.util.SessionHelper.setSessionCurrentRequest;
import static org.neociclo.odetteftp.util.SessionHelper.setSessionFileChannel;
import static org.neociclo.odetteftp.util.SessionHelper.setSessionOutgoingDataExchangeBuffer;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_RECORD_SIZE;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.ProtocolHandler;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.oftplet.AnswerReasonInfo;
import org.neociclo.odetteftp.oftplet.EndFileResponse;
import org.neociclo.odetteftp.oftplet.EndSessionReasonInfo;
import org.neociclo.odetteftp.oftplet.Oftplet;
import org.neociclo.odetteftp.oftplet.OftpletListener;
import org.neociclo.odetteftp.oftplet.OftpletSpeaker;
import org.neociclo.odetteftp.oftplet.ServerOftplet;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DeliveryNotification.EndResponseType;
import org.neociclo.odetteftp.protocol.data.AbstractMapping;
import org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13;
import org.neociclo.odetteftp.security.PasswordAuthenticationCallback;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.security.SecurityContext;
import org.neociclo.odetteftp.util.ProtocolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 */
public abstract class DefaultHandler implements ProtocolHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHandler.class);

    // BOTH STATE METHODS
    // -------------------------------------------------------------------------

    public void abnormalRelease(OdetteFtpSession session, EndSessionReason error, String errorText)
            throws OdetteFtpException {
        protocolRelease(session, error, errorText);
        throw new EndSessionException(error);
    }

    public void abort(OdetteFtpSession session, EndSessionReason error, String errorText) throws OdetteFtpException {
        protocolRelease(session, error, errorText);
    }

    public void startSessionReceived(OdetteFtpSession session, CommandExchangeBuffer ssid) throws OdetteFtpException {

        Oftplet oftplet = getSessionOftplet(session);

        // Protocol Sequence step 2
        if (session.isResponder()) {
            /* Authenticate the Initiator peer identification. */
            boolean authenticated = startSessionPasswordAuthentication(session, ssid, true);
            if (!authenticated) {
            	return;
            }

            /*
             * After authorization, keep user code in session context.
             */
            String ssidcode = ssid.getStringAttribute(SSIDCODE_FIELD);
            session.setUserCode(ssidcode);

			/*
			 * Let the server Oftplet to setup custom session parameters based
			 * on authenticated user's OID.
			 */
            if (oftplet instanceof ServerOftplet) {
            	((ServerOftplet) oftplet).configure();
            }

            /*
             * Get and evaluate session parameters from Initiator SSID command,
             * handshake on the agreed value for this session.
             */
            responderSendStartSession(session, ssid);
        }
        /* Protocol Sequence Step 3 */
        else {
            /*
             * Configure Initiator's session context with session parameters
             * agreed from the Responder peer, getting the values from the
             * received SSID command.
             */
            initiatorStartSessionReceived(session, ssid);
        }

        oftplet.onSessionStart();

    }

    // LISTENER STATE METHODS
    // -------------------------------------------------------------------------

    /**
     * Method called after the SSIDs are fully exchanged.
     */
    public void afterStartSession(OdetteFtpSession session) throws OdetteFtpException {
        if (isInitiator(session)) {
            /*
             * Perform Speaker's tasks of starting file transmit and acknowledgment
             * issuing. After the session handshaking.
             */
            speakerTransmitRequests(session);
        }
    }

    public void changeDirectionReceived(OdetteFtpSession session) throws OdetteFtpException {

        // Change odette-ftp entity state in session context.
        session.changeState();

        LOGGER.debug("[{}] CD received. Odette FTP entity state changed: LISTENER --> SPEAKER", session);

        // Perform Speaker's tasks - transmit Files and End Responses
        speakerTransmitRequests(session);

    }

    // File Receiving 
    // -------------------------------------------------------------------------

    public void startFileReceived(OdetteFtpSession session, CommandExchangeBuffer sfid) throws OdetteFtpException {

        Oftplet oftplet = getSessionOftplet(session);
        final OftpletListener oftpletListener = oftplet.getListener();

        // validate the transfer mode and if oftplet listener is provided
        if (!isReceivingSupported(session)) {

            String incompatibleModeErr = "Receiving is not supported. Bad Start File command.";
            LOGGER.error("[{}] SFID received. {}", session, incompatibleModeErr);

            abnormalRelease(session, EndSessionReason.INCOMPATIBLE_MODE, incompatibleModeErr);

        } else if (oftpletListener == null) {

            String resNotAvailableErr = "File Receive failed. Oftplet did not provided a Listener.";
            LOGGER.error("[{}] SFID received. {} Oftplet: {}", new Object[] {session, resNotAvailableErr, oftplet});

            abnormalRelease(session, EndSessionReason.RESOURCES_NOT_AVAIABLE, resNotAvailableErr);

        }

        final DefaultVirtualFile virtualFile = buildVirtualFileObject(session, sfid);

        /* Fire exception when file size is over the protocol limit. */
        long fileSize = virtualFile.getSize();
        if (fileSize > protocolMaxFileSizeSupported()) {
            // reject file receiving
            LOGGER.warn("[{}] SFID received. Incoming file rejected. File size exceed: {}", session, fileSize);

            CommandExchangeBuffer fileSizeExceed = buildStartFileNegativeAnswerCommand(AnswerReason.FILE_SIZE_EXCEED,
                    "Start File exceed the max supported protocol file size: " + protocolMaxFileSizeSupported(),
                    false);
            session.write(fileSizeExceed);

            return;
        }

        /*
         * Notify the odette-ftp support provider about the start file request
         * and expect in return the incoming Virtual File to be opened.
         */
        StartFileResponse response = oftpletListener.acceptStartFile(virtualFile);

        /* Assure a Virtual File were returned by the support implementation. */
        if (response == null) {

            LOGGER.warn("[{}] SFID received. File Receive failed. ",
                    session);

            CommandExchangeBuffer sfnaUnspecified = buildStartFileNegativeAnswerCommand(AnswerReason.UNSPECIFIED,
            		"Unable to handle the Start File request.", true);
            session.write(sfnaUnspecified);

            return;

        } else if (!response.accepted()) {

            LOGGER.debug("[{}] SFID received. Receive File rejected.", session);

            AnswerReason rejectReason = response.getReason();
            boolean retryLater = response.retryLater();

            if (rejectReason == null) {
                LOGGER.warn("[{}] Oftplet listener accept request returned a null Reason. Using UNSPECIFED and " +
                        "retryLater=true", session);
                rejectReason = AnswerReason.UNSPECIFIED;
                retryLater = true;
            }

            CommandExchangeBuffer sfnaRejected = buildStartFileNegativeAnswerCommand(rejectReason,
                    "Start File rejected.", retryLater);
            session.write(sfnaRejected);

            return;
        }

        virtualFile.setFile(response.getFile());

        // Open file channel and keep object in session
        FileChannel fileChannel = null;
        try {
            fileChannel = (new FileOutputStream(virtualFile.getFile(), true)).getChannel();
            LOGGER.debug("[{}] SFID received. Output file channel opened: {}", session, virtualFile);
        } catch (FileNotFoundException e) {

            LOGGER.error("[" + session + "] SFID received. Receive File failed. Cannot open the specified file: "
                    + virtualFile.getFile(), e);

            String openFailureText = "Cannot open the output file on local system.";

            CommandExchangeBuffer sfnaOpenFailure = buildStartFileNegativeAnswerCommand(ACCESS_METHOD_FAILURE,
                    openFailureText, true);
            session.write(sfnaOpenFailure);

            oftpletListener.onReceiveFileError(virtualFile, new AnswerReasonInfo(AnswerReason.ACCESS_METHOD_FAILURE,
                    openFailureText));

            return;
        }

        // Warn about the bad restart offset set in response 
        if (!session.isRestartSupported() && response.getRestartOffset() > 0) {
            LOGGER.warn("[{}] SFID received. Session doesn't support Restart capability, negotiating answer count as " +
                    "0. Offset in start file response was: {}", session, response.getRestartOffset());
        }

        /* Reply with a positive answer indicating the restart offset. */
        final long answerCount = (session.isRestartSupported() ? Math.min(virtualFile.getRestartOffset(), response
                .getRestartOffset()) : 0);

        // position file output channel at restart offset
        if (answerCount > 0) {
            long position = ProtocolUtil.computeOffsetFilePosition(answerCount, virtualFile.getRecordFormat(),
                    virtualFile.getRecordSize());

            try {
                if (fileChannel.size() > position) {
                    fileChannel.truncate(position);
                } else {
                    fileChannel.position(position);
                }
            } catch (IOException e) {

                LOGGER.error("[" + session + "] SFID received. Receive File failed. Cannot truncate/position file to "
                        + "restart (offset=" + position + "): " + virtualFile, e);

                String restartFailedText = "Cannot truncate/position output file to restart at: " + position;

                CommandExchangeBuffer sfnaRestart = buildStartFileNegativeAnswerCommand(ACCESS_METHOD_FAILURE,
                        restartFailedText, true);
                session.write(sfnaRestart);

                oftpletListener.onReceiveFileError(virtualFile, new AnswerReasonInfo(AnswerReason.ACCESS_METHOD_FAILURE,
                        restartFailedText));

                return;
            }

        }
        
        setSessionFileChannel(session, fileChannel);

        /* Keep in the odette-ftp session the incoming file request. */
        setSessionCurrentRequest(session, virtualFile);

        // reset incoming bytes transferred and credits counter
        session.setIncomingBytesTransfered(0);
        resetIncomingCredits(session);

        LOGGER.info("[{}] SFID received. Replied with positive reponse with answerCount={}. Virtual File: {}",
                new Object[] { session, answerCount, virtualFile });

        Runnable onReceiveStart = new Runnable() {
            public void run() {
                oftpletListener.onReceiveFileStart(virtualFile, answerCount);
            }
        };

        CommandExchangeBuffer sfpa = buildStartFilePositiveAnswerCommand(answerCount);
        session.write(sfpa, onReceiveStart);
    }

    public void dataBufferReceived(OdetteFtpSession session, DataExchangeBuffer data) throws OdetteFtpException {

        Oftplet oftplet = getSessionOftplet(session);
        OftpletListener oftpletListener = oftplet.getListener();

        VirtualFile virtualFile = (VirtualFile) getSessionCurrentRequest(session);
        FileChannel fileChannel = getSessionFileChannel(session);

        /* Consume window credits on odette-ftp session. */
        consumeIncomingCredits(session);

        /*
         * Use the appropriate Virtual File mapping strategy to write down the
         * flatten data buffer.
         */
        OdetteFtpVersion version = session.getVersion();
        boolean compression = session.isCompressionSupported();

        RecordFormat recordFormat = virtualFile.getRecordFormat();

        AbstractMapping mapping = AbstractMapping.getInstance(version, compression, recordFormat);
        long totalBytesWritten = mapping.writeData(virtualFile, data, fileChannel);

        session.setIncomingBytesTransfered(totalBytesWritten);
        oftpletListener.onDataReceived(virtualFile, totalBytesWritten);

        /*
         * Data Flow syncing with the remote peer sending a Set Credit command
         * when the credits value in odette-ftp context is all consumed.
         */
        if (session.getIncomingCredits() <= 0) {
            CommandExchangeBuffer cdt = buildSetCreditCommand();
            session.write(cdt);

            resetIncomingCredits(session);
        }
    }

    public void endFileReceived(OdetteFtpSession session, CommandExchangeBuffer efid) throws OdetteFtpException {

        Oftplet oftplet = getSessionOftplet(session);
        OftpletListener oftpletListener = oftplet.getListener();

        long recordCount = Long.parseLong(efid.getStringAttribute(EFIDRCNT_FIELD));
        long unitCount = Long.parseLong(efid.getStringAttribute(EFIDUCNT_FIELD));

        // Retrieve and then clear the current Virtual File in the session object
        VirtualFile virtualFile = (VirtualFile) getSessionCurrentRequest(session);
        setSessionCurrentRequest(session, null);

        FileChannel fileChannel = getSessionFileChannel(session);

        long fileUnitCount = 0;

        // Get the total octets count from the Virtual File received
        try {
            fileUnitCount = fileChannel.size();
        } catch (IOException e) {
        	// ignore
        }

        // close output stream file channel
        closeIoChannelOnEndFileAnswer(fileChannel, "EFID", virtualFile, session);

        // calculate the Virtual File record/block count based on recordFormat and recordSize
        long fileRecordCount = computeVirtualFileRecordCount(fileUnitCount, virtualFile.getRecordFormat(), virtualFile
                .getRecordSize());

        RecordFormat fileFormat = virtualFile.getRecordFormat();

        /*
         * Record Count validating: TEXTFILE and UNSTRUCTURED formats must have
         * EFIDRCNT equal to zero. FIXED and VARIABLE formats must have EFIDRCNT
         * exactly equals.
         */
        boolean invalidTextUnstructured = (recordCount != 0) && (fileFormat == RecordFormat.TEXTFILE ||
                fileFormat == RecordFormat.UNSTRUCTURED);

        boolean invalidFixedVariable = (recordCount != fileRecordCount)
                && (fileFormat == RecordFormat.FIXED || fileFormat == RecordFormat.VARIABLE);

        if (invalidTextUnstructured || invalidFixedVariable) {

            LOGGER.warn("[{}] EFID received. Invalid record count (recordCount={}, recordFormat={}) on virtual file:",
                    new Object[] {session, recordCount, fileFormat, virtualFile});

            String invalidRecordCountText = "Invalid record count [" + recordCount + "] for record format ["
                    + fileFormat + "].";

            CommandExchangeBuffer invalidRecordCount = buildEndFileNegativeAnswerCommand(INVALID_RECORD_COUNT,
                    invalidRecordCountText);
            session.write(invalidRecordCount);

            oftpletListener.onReceiveFileError(virtualFile, new AnswerReasonInfo(AnswerReason.INVALID_RECORD_COUNT,
                    invalidRecordCountText));

            return;
        }

        /* Unit Count validating. */
        if (unitCount != fileUnitCount) {

            LOGGER.warn("[{}] EFID received. Invalid byte count (received file unitCount={}) on virtual file:",
                    new Object[] {session, fileUnitCount, virtualFile});

            String invalidByteCountText = "Invalid unit count. Received file unit count is: " + fileUnitCount;

            CommandExchangeBuffer invalidRecordCount = buildEndFileNegativeAnswerCommand(INVALID_BYTE_COUNT,
                    invalidByteCountText);
            session.write(invalidRecordCount);

            oftpletListener.onReceiveFileError(virtualFile, new AnswerReasonInfo(AnswerReason.INVALID_BYTE_COUNT,
                    invalidByteCountText));

            return;
        }

        /*
         * Notify OdetteFtpSupport provider on End File
         */
        EndFileResponse endFileResponse = oftpletListener.onReceiveFileEnd(virtualFile, recordCount, unitCount);

        if (endFileResponse.accepted()) {
	        boolean changeDirection = endFileResponse.changeDirection();
	
	        /* Send Positive Answer. */
	        CommandExchangeBuffer efpa = buildEndFilePositiveAnswerCommand(changeDirection);
	        session.write(efpa);
        } else {
        	/* Send Negative Answer */
            CommandExchangeBuffer efna = buildEndFileNegativeAnswerCommand(endFileResponse.getReason(),
                    endFileResponse.getReasonText());
            session.write(efna);
        }
    }

    // SPEAKER STATE METHODS
    // -------------------------------------------------------------------------

    public void sessionConnected(OdetteFtpSession session) throws OdetteFtpException {

        LOGGER.info("[{}] Network connection established.", session);

        // The first message must be sent by the Responder.
        if (session.isResponder()) {
            // send a start session ready message command to the caller
            CommandExchangeBuffer ssrm = CommandBuilder.readyMessage();
            session.write(ssrm);
        }

    }

    public void readyToReceiveReceived(OdetteFtpSession session) throws OdetteFtpException {

        /*
         * Indicate that the End Response is transmitted and fully acknowledged
         * by the other part.
         */

        Oftplet oftplet = getSessionOftplet(session);

        OftpletSpeaker speaker = oftplet.getSpeaker();
        if (speaker != null) {
            
            LOGGER.info("[{}] RTR received. Delivery notification is fully transmitted. Invoking oftplet: {}.",
                    session, oftplet);
    
            DeliveryNotification notif = (DeliveryNotification) getSessionCurrentRequest(session);
            speaker.onNotificationSent(notif);

        } else {
            // SPEAKER is expected since a delivery notification was sent
            LOGGER.warn("[{}] RTR received. Unexpected null Oftplet speaker handler returned. Oftplet: {}", session,
                    oftplet);
        }

        /*
         * In order to avoid congestion between two adjacent nodes caused by a
         * continuous flow of EERPs and NERPs, a Ready To Receive (RTR) command
         * is provided. The RTR acts as an EERP/NERP acknowledgment for flow
         * control but has no end-to-end significance.
         */
        speakerTransmitRequests(session);

    }

    public void setCreditReceived(OdetteFtpSession session, CommandExchangeBuffer cdt) throws OdetteFtpException {

        // Reset the credit counter in odette-ftp context.
        LOGGER.info("[{}] CDT received. Resetting session window credit counter.", session);
        resetOutgoingCredits(session);

        // Proceed with file transmission.
        speakerSendData(session);

    }

    // File Sending
    // -------------------------------------------------------------------------

    /**
     * Do start file transmission.
     * 
     * @param session
     * @param exchange
     * @throws OdetteFtpException
     */
    protected void speakerStartFile(OdetteFtpSession session, VirtualFile vf)
            throws OdetteFtpException {

        session.setOutgoingBytesTransfered(0);

        /* Transmit the SFID command. */
        CommandExchangeBuffer sfid = buildStartFileCommand(session, vf);
        session.write(sfid);

    }

    protected VirtualFile normalizeVirtualFile(OdetteFtpSession session, VirtualFile vf) {

        String dsn = (vf.getDatasetName() == null ? (vf.getFile() == null ? null : vf.getFile().getName()) : vf
                .getDatasetName());
        Date dateTime = (vf.getDateTime() == null ? (vf.getFile() == null ? null
                : new Date(vf.getFile().lastModified())) : vf.getDateTime());

        if (dsn == null) {
            throw new NullPointerException("Virtual File object has null Dataset Name");
        } else if (dateTime == null) {
            throw new NullPointerException("Virtual File object has null Date/Time");
        }

        int dsnLength = ReleaseFormatVer13.SFID_V13.getField(SFIDDSN_FIELD).getSize();
        if (dsn.length() > dsnLength) {
        	dsn = dsn.substring(0, dsnLength);
        }

        String orig = (vf.getOriginator() == null ? session.getUserCode() : vf.getOriginator());
        String dest = (vf.getDestination() == null ? session.getUserCode() : vf.getDestination());

        RecordFormat recordFormat = (vf.getRecordFormat() == null ? RecordFormat.UNSTRUCTURED : vf.getRecordFormat());
        int recordSize = (recordFormat == RecordFormat.UNSTRUCTURED || recordFormat == RecordFormat.TEXTFILE ? 0 :
        	Math.abs( Math.min(vf.getRecordSize(), DEFAULT_RECORD_SIZE) ) );
        long restartOffset = (session.isRestartSupported() ? Math.max(vf.getRestartOffset(), 0) : 0);

        long unitCount = (vf.getFile() == null ? 0 : vf.getFile().length());
        long fileSize = Math.max(vf.getSize(), ProtocolUtil.computeVirtualFileSize(unitCount));


        // return the normalized virtual file
        DefaultNormalizedVirtualFile n = new DefaultNormalizedVirtualFile(vf);
        n.setDatasetName(dsn);
        n.setDateTime(dateTime);
        n.setOriginator(orig);
        n.setDestination(dest);
        n.setRecordFormat(recordFormat);
        n.setRecordSize(recordSize);
        n.setSize(fileSize);
        n.setRestartOffset(restartOffset);

        n.setFile(vf.getFile());

        return n;
    }

    public void startFilePositiveAnswerReceived(OdetteFtpSession session, CommandExchangeBuffer sfpa)
            throws OdetteFtpException {

        Oftplet oftplet = getSessionOftplet(session);
        OftpletSpeaker oftpletSpeaker = oftplet.getSpeaker();

        NormalizedVirtualFile normalizedVirtualFile = (NormalizedVirtualFile) getSessionCurrentRequest(session);

        long restartOffset = normalizedVirtualFile.getRestartOffset();

        /* Validate answer count and restart support. */
        long answerCount = Long.parseLong(sfpa.getStringAttribute(SFPAACNT_FIELD));
        if ((answerCount > 0) && !session.isRestartSupported()) {
            String restartNotSupported = "Restart is not supported; answer count: " + answerCount;
            LOGGER.error("[{}] SFPA received. {}", session, restartNotSupported);
            abnormalRelease(session, EndSessionReason.PROTOCOL_VIOLATION, restartNotSupported);
        } else if (answerCount > restartOffset) {
            String illegalAnswerCount = "Restart offset negotiation failed. Illegal answer count: " + answerCount;
            LOGGER.error("[{}] SFPA received. {}", session, illegalAnswerCount);
            abnormalRelease(session, EndSessionReason.PROTOCOL_VIOLATION, illegalAnswerCount);
        }

        LOGGER.info("[{}] SFPA received. Answer count is: {}", session, answerCount);

        /* Set Virtual File restart offset. */
        if ((answerCount > 0) && (answerCount != restartOffset)) {
            session.setOutgoingOffset(answerCount);
        }

        // Reset window credit counter for the new exchange
        resetOutgoingCredits(session);

        // Open file channel and keep object in session
        FileChannel fileChannel = null;
        try {
            fileChannel = (new FileInputStream(normalizedVirtualFile.getFile())).getChannel();
            //PS: setting the channel in the session immediately. So the channel can also be closed from outside in case of exception.
            setSessionFileChannel(session, fileChannel);
        } catch (FileNotFoundException e) {

            LOGGER.error("[" + session + "] Send File failed. Cannot open file specified in the Virtual File: "
                    + normalizedVirtualFile.getFile(), e);

            String fileNotFoundText = "Cannot open input file on local system.";

			oftpletSpeaker.onSendFileError(normalizedVirtualFile, new AnswerReasonInfo(
					AnswerReason.ACCESS_METHOD_FAILURE, fileNotFoundText), true);

            abnormalRelease(session, EndSessionReason.RESOURCES_NOT_AVAIABLE, fileNotFoundText);
        }

        // Position file at the proper restart offset
        if (answerCount > 0) {
            RecordFormat recordFormat = normalizedVirtualFile.getRecordFormat();
            int recordSize = normalizedVirtualFile.getRecordSize();
            if (recordSize <= 0 || recordFormat == RecordFormat.TEXTFILE || recordFormat == RecordFormat.UNSTRUCTURED) {
                recordSize = 1024;
            }

            long fileOffset = (answerCount * recordSize);

            if (fileOffset > 0) {
                try {
                    //PS: Only setting the position here. Truncate makes no sense in case of reading
                    fileChannel.position(fileOffset);
//                    if (fileChannel.size() > fileOffset) {
//                        fileChannel.truncate(fileOffset);
//                    } else {
//                        fileChannel.position(fileOffset);
//                    }

                    //PS: we have to set those bytes as already transfered, so that our EFIDUCNT is correct!!!
                    session.setOutgoingBytesTransfered(fileOffset);
                    
                    //PS: Catching all Exceptions. fileChannel.positions() throws three kinds of exceptions!
                } catch (Exception e) {
    
                    String restartFailedText = "Cannot truncate/position file to restart at: " + fileOffset;
                    LOGGER.error("[" + session + "] SFPA received. Send File failed. " + restartFailedText, e);
    
					oftpletSpeaker.onSendFileError(normalizedVirtualFile, new AnswerReasonInfo(
							AnswerReason.ACCESS_METHOD_FAILURE, restartFailedText), true);
    
                    abnormalRelease(session, EndSessionReason.RESOURCES_NOT_AVAIABLE, restartFailedText);
    
                }
            }

        }

        // Allocate data buffer in memory to keep in session
        DataExchangeBuffer dataBuffer = new DataExchangeBuffer(session.getDataBufferSize());
        setSessionOutgoingDataExchangeBuffer(session, dataBuffer);

        // Indicate the outgoing Start File to the oftplet

        oftpletSpeaker.onSendFileStart(normalizedVirtualFile, answerCount);

        // Begin data buffer transmission
        speakerSendData(session);

    }

    public void startFileNegativeAnswerReceived(OdetteFtpSession session, CommandExchangeBuffer sfna)
            throws OdetteFtpException {

        boolean retryLater = valueOfYesNo(sfna.getStringAttribute(SFNARRTR_FIELD));

        NormalizedVirtualFile normalizedVirtualFile = (NormalizedVirtualFile) getSessionCurrentRequest(session);

        // Clear current odette-ftp outgoing exchange request
        setSessionCurrentRequest(session, null);

        /* Indicate the odette-ftp support provider it won't start. */
        Oftplet oftplet = getSessionOftplet(session);
        OftpletSpeaker oftpletSpeaker = oftplet.getSpeaker();

        AnswerReasonInfo reasonInfo = buildAnswerReasonInfoObject(sfna);
        oftpletSpeaker.onSendFileError(normalizedVirtualFile, reasonInfo, retryLater);

        /*
         * Perform Speaker's tasks of starting file transmit and acknowledgment
         * issuing. After received start file negative answer.
         */
        speakerTransmitRequests(session);

    }

    /**
     * Perform data buffer transmission of the file transfer opened.
     * 
     * @param session
     * @throws OdetteFtpException
     */
    protected void speakerSendData(OdetteFtpSession session) throws OdetteFtpException {

        Oftplet oftplet = getSessionOftplet(session);
        final OftpletSpeaker oftpletSpeaker = oftplet.getSpeaker();

        DataExchangeBuffer dataBuffer = getSessionOutgoingDataExchangeBuffer(session);
        FileChannel fileChannel = getSessionFileChannel(session);

        /* Use the appropriate Virtual File mapping strategy. */
        OdetteFtpVersion version = session.getVersion();
        boolean compression = session.isCompressionSupported();

        final NormalizedVirtualFile virtualFile = (NormalizedVirtualFile) getSessionCurrentRequest(session);
        RecordFormat recordFormat = virtualFile.getRecordFormat();

        AbstractMapping mapping = AbstractMapping.getInstance(version, compression, recordFormat);

        /*
         * Ready the Virtual File stream and send the data buffer while
         * odette-ftp context has credits, until reach the end of stream to send
         * the End File indication or having all credits consumed and expect for
         * the Set Credit command.
         */
        while (session.getOutgoingCredits() > 0) {

            // Consume window credits in the Oftp session
            consumeOutgoingCredits(session);

            /* Read data buffer from the stream. */
            boolean endOfStream = mapping.readData(virtualFile, fileChannel, dataBuffer);

            if (dataBuffer.getUnitCount() > 0 ) {
	            final long overallSentBytes = session.getOutgoingBytesTransfered() + dataBuffer.getUnitCount();
	
	            // Update the Oftplet on each data transmission (total bytes sent)
	            Runnable progressCallback = new Runnable() {
	                public void run() {
	                    oftpletSpeaker.onDataSent(virtualFile.getOriginalVirtualFile(), overallSentBytes);
	                }
	            };
	
	            /* Transmit the data exchange buffer. */
	            session.write(dataBuffer, progressCallback);
	
	            /*
	             * After data buffer transmit, update the total bytes sent in the
	             * context.
	             */
	            session.setOutgoingBytesTransfered(overallSentBytes);
	            
	            dataBuffer.clear();
            }

            /*
             * Submit the End File indication when reach the end of stream, even
             * though there are still credits. So stop looping.
             */
            if (endOfStream && session.getOutgoingCredits() > 0) {
                speakerEndFile(session, virtualFile);
                break;
            }

        }
        
    }

    protected void speakerEndFile(OdetteFtpSession session, VirtualFile virtualFile) throws OdetteFtpException {

        // Get the total octets count from the Virtual
        
        long unitCount = session.getOutgoingBytesTransfered();
//        long unitCount = virtualFile.getFile().length();

        long recordCount = computeVirtualFileRecordCount(unitCount, virtualFile.getRecordFormat(), virtualFile
                .getRecordSize());

        /* Construct and send the End File indication command. */
        CommandExchangeBuffer efid = buildEndFileCommand(recordCount, unitCount);
        session.write(efid);

    }

    public void endFileNegativeAnswerReceived(OdetteFtpSession session, CommandExchangeBuffer efna)
            throws OdetteFtpException {

    	NormalizedVirtualFile normalizedVirtualFile = (NormalizedVirtualFile) getSessionCurrentRequest(session);

    	// Close file channel
        FileChannel fileChannel = getSessionFileChannel(session);
        closeIoChannelOnEndFileAnswer(fileChannel, "EFNA", normalizedVirtualFile, session);

        // Clear current odette-ftp outgoing exchange request
        setSessionCurrentRequest(session, null);
        session.setOutgoingBytesTransfered(0);

        Oftplet oftplet = getSessionOftplet(session);
        OftpletSpeaker oftpletSpeaker = oftplet.getSpeaker();

        AnswerReasonInfo reasonInfo = buildAnswerReasonInfoObject(efna);

        LOGGER.info("[{}] EFNA received. Invoking OftpletSpeaker onSendFileError() with: {}", session, reasonInfo);

        oftpletSpeaker.onSendFileError(normalizedVirtualFile, reasonInfo, true);

        /*
         * Perform Speaker's tasks of starting file transmit and acknowledgment
         * issuing. After received end file negative answer.
         */
        speakerTransmitRequests(session);

    }

	private void closeIoChannelOnEndFileAnswer(FileChannel fileChannel, String commandName, VirtualFile virtualFile, OdetteFtpSession session) {

        Oftplet oftplet = getSessionOftplet(session);
        OftpletListener oftpletListener = oftplet.getListener();

		try {
			fileChannel.force(true);
			fileChannel.close();
		} catch (IOException e) {
            LOGGER.error("[" + session + "] " +  commandName + " received. Error closing output file channel: " + 
            		virtualFile, e);

            String errorClosingText = "Failed to close the output file.";

            CommandExchangeBuffer efnaErrorClosing = buildEndFileNegativeAnswerCommand(ACCESS_METHOD_FAILURE,
                    errorClosingText);
            session.write(efnaErrorClosing);

            oftpletListener.onReceiveFileError(virtualFile, new AnswerReasonInfo(AnswerReason.ACCESS_METHOD_FAILURE,
                    errorClosingText));

            return;
		}
	}

	public void endFilePositiveAnswerReceived(OdetteFtpSession session, CommandExchangeBuffer efpa)
            throws OdetteFtpException {

    	NormalizedVirtualFile normalizedVirtualFile = (NormalizedVirtualFile) getSessionCurrentRequest(session);

    	// Close file channel
        FileChannel fileChannel = getSessionFileChannel(session);
        closeIoChannelOnEndFileAnswer(fileChannel, "EFPA", normalizedVirtualFile, session);

        // Indicate that the outgoing file transfer has neded to the Oftplet
        Oftplet oftplet = getSessionOftplet(session);
        OftpletSpeaker oftpletSpeaker = oftplet.getSpeaker();

        oftpletSpeaker.onSendFileEnd(normalizedVirtualFile);

        // Clear current odette-ftp outgoing exchange request
        setSessionCurrentRequest(session, null);
        session.setOutgoingBytesTransfered(0);

        /*
         * Attend to the Listener request on turning direction, otherwise
         * continue Speaker's tasks of issuing Acknowledgments and transmitting
         * files.
         */
        boolean changeDirection = valueOfYesNo(efpa.getStringAttribute(EFPACD_FIELD));

        if (changeDirection) {
            speakerChangeDirection(session);
        } else {
            // continue sending outgoing requests
            speakerTransmitRequests(session);
        }

    }

    // PROTOCOL METHODS
    // -------------------------------------------------------------------------

    public void readyMessageReceived(OdetteFtpSession session) throws OdetteFtpException {

        LOGGER.info("[{}] SSRM received.", session);

        String userCode = null;
        String userPassword = null;

        PasswordCallback pwdCallback = new PasswordCallback();
        if (handleCallback(session, pwdCallback)) {
            userCode = pwdCallback.getUsername();
            userPassword = pwdCallback.getPassword();
        } else {
            abnormalRelease(session, EndSessionReason.UNSPECIFIED_ABORT, "User code and password were not provided.");
        }

        session.setUserCode(userCode);

        /* Start Session Phase */
        CommandExchangeBuffer ssid = buildStartSessionCommand(userCode, userPassword, session
                .getUserData(), session);

        session.write(ssid);
    }

    protected void speakerDeliveryNotification(OdetteFtpSession session, DeliveryNotification notif)
            throws OdetteFtpException {

        OdetteFtpVersion version = session.getVersion();
        if (notif.getType() == EndResponseType.NEGATIVE_END_RESPONSE
                && version.isEqualOrEarlier(OdetteFtpVersion.OFTP_V13)) {
            /* Cannot handle or issue NoDeliveryAck in ODETTE-FTP version 1.3. */
            String msg = "Negative End Response is not supported in protocol version 1.3. Cannot transmit request: "
                    + notif;
            LOGGER.error("[{}] " + msg, session);
            throw new OdetteFtpException(msg);
        }

        CommandExchangeBuffer eerp = buildDeliveryNotificationCommand(notif);
        session.write(eerp);

    }

    public void endToEndResponseReceived(OdetteFtpSession session, CommandExchangeBuffer eerp)
            throws OdetteFtpException {

        /* Parse End-to-End Response info. */
        DeliveryNotification notif = buildEndToEndResponse(eerp);

        /*
         * Indicate the delivery acknowledgment on the odette-ftp support
         * provider.
         */
        Oftplet oftplet = getSessionOftplet(session);
        OftpletListener oftpletListener = oftplet.getListener();

        if (oftpletListener != null) {
            LOGGER.debug("[{}] EERP received. Invoking the onNotificationReceived() on the Oftplet Listener: {}",
                    session, oftpletListener);
            oftpletListener.onNotificationReceived(notif);
        } else {
            LOGGER.warn("[{}] EERP received. Cannot indicate to the Oftplet Listener - returned null: {}", session,
                    oftplet);
        }

        /* Reply with a Ready to Receive. */
        CommandExchangeBuffer rtr = buildReadyToReceiveCommand();
        session.write(rtr);

    }

    public void protocolRelease(final OdetteFtpSession session, EndSessionReason reason, String reasonText) {

        /* Indicate the odette-ftp support provider on session closed. */
        final Oftplet oftplet = getSessionOftplet(session);

        /*
         * Send an End Session command and await for the configured timeout
         * period for complete the sending
         */
        CommandExchangeBuffer esid = buildEndSessionCommand(reason, reasonText);

        // terminate the connection after all queued write requests are done
        Runnable closeOnComplete = new Runnable() {
            public void run() {
                oftplet.onSessionEnd();
                session.close();
            }
        };

        session.write(esid, closeOnComplete);
    }

    public void endSessionReceived(OdetteFtpSession session, CommandExchangeBuffer esid) throws OdetteFtpException {

        // indicate to the oftplet that session has ended
        Oftplet oftplet = getSessionOftplet(session);

        LOGGER.debug("[{}] ESID received. Invoking method onSessionEnd() on the Oftplet object: {}", session, oftplet);
        oftplet.onSessionEnd();

        EndSessionReasonInfo reasonInfo = buildEndSessionReasonInfoObject(esid);

        /* Raise exception when it's not normal termination. */
        EndSessionReason reason = reasonInfo.getEndSessionReason();
        if (reason != EndSessionReason.NORMAL_TERMINATION) {
        	// exceptionHandler will close the session after handling exception
            String reasonText = reasonInfo.getReasonText();
            if (reasonText == null) {
                reasonText = "Abnormal session end received: " + reason.name();
            }
            throw new EndSessionException(reason, reasonText);
        } else {
        	//close the session immediately
        	session.close();
        }

    }

    /**
     * The Initiator handle the returned Start Session Identification command to
     * perform its setup and complete the Start Session Phase.
     */
    protected void initiatorStartSessionReceived(OdetteFtpSession session, CommandExchangeBuffer ssid) throws OdetteFtpException {

        int ssidlev = Integer.parseInt(ssid.getStringAttribute(SSIDLEV_FIELD));
        int ssidsdeb = Integer.parseInt(ssid.getStringAttribute(SSIDSDEB_FIELD));
        String ssidcode = ssid.getStringAttribute(SSIDCODE_FIELD);
        TransferMode ssidsr = TransferMode.parse(ssid.getStringAttribute(SSIDSR_FIELD));
        boolean ssidcmpr = valueOfYesNo(ssid.getStringAttribute(SSIDCMPR_FIELD));
        boolean ssidrest = valueOfYesNo(ssid.getStringAttribute(SSIDREST_FIELD));
        boolean ssidspec = valueOfYesNo(ssid.getStringAttribute(SSIDSPEC_FIELD));
        int ssidcred = Integer.parseInt(ssid.getStringAttribute(SSIDCRED_FIELD));
        String ssiduser = ssid.getStringAttribute(SSIDUSER_FIELD);

        /* Procedure to perform parameters validity checking */
        checkSessionParamsViolation(ssidsdeb, ssidcred, ssidsr, ssidspec, session);

        Oftplet oftplet = getSessionOftplet(session);

        OdetteFtpVersion version = OdetteFtpVersion.parse(ssidlev);
        if (version.isOlder(session.getVersion())) {
            String incompatibleVersion = "Incompatible protocol version: " + version;
            LOGGER.warn("[{}] SSID received. {}", session, incompatibleVersion);
            abnormalRelease(session, EndSessionReason.INCOMPATIBLE_MODE, incompatibleVersion);
            return;
        } else {
            if (oftplet.isProtocolVersionSupported(version)) {
                // agree to use a lower odette-ftp protocol version in session
                session.setVersion(version);
            } else {
                String versionNotSupported = "Required protocol version is not supported: " + version;
                LOGGER.warn("[{}] SSID received. {}", session, versionNotSupported);
                abnormalRelease(session, EndSessionReason.INCOMPATIBLE_MODE, versionNotSupported);
                return;
            }
        }

        /*
         * Raise an incompatible mode error when response parameters values are
         * greater than those from the request (filled up from session context
         * attributes).
         * --------------------------------------------------------------------
         */

        String err;

        if (ssidsdeb > session.getDataBufferSize()) {
            err = "Invalid Data Exchange Buffer size: " + ssidsdeb;
            LOGGER.error("[{}] Session setup failed. {}", session, err);
            abnormalRelease(session, EndSessionReason.INCOMPATIBLE_MODE, err);
        }

        if (ssidcred > session.getWindowSize()) {
            err = "Invalid Window Credit size: " + ssidcred;
            LOGGER.error("[{}] Session setup failed. {}", session, err);
            abnormalRelease(session, EndSessionReason.INCOMPATIBLE_MODE, err);
        }

        if (ssidcmpr && (ssidcmpr != session.isCompressionSupported())) {
            err = "Buffer data compression is not supported.";
            LOGGER.error("[{}] Session setup failed. {}", session, err);
            abnormalRelease(session, EndSessionReason.INCOMPATIBLE_MODE, err);
        }

        if (ssidrest && (ssidrest != session.isRestartSupported())) {
            err = "File transfer restart/checkpoint is not supported.";
            LOGGER.error("[{}] Session setup failed. {}", session, err);
            abnormalRelease(session, EndSessionReason.INCOMPATIBLE_MODE, err);
        }

        // authenticate responder's ID with the PasswordAuthenticationCallback 
        startSessionPasswordAuthentication(session, ssid, false);

        /* Set up agreed values in preferences of odette-ftp session context. */

        session.setDataBufferSize(ssidsdeb);
        session.setWindowSize(ssidcred);
        session.setCompressionSupport(ssidcmpr);
        session.setRestartSupport(ssidrest);
        session.setSpecialLogic(ssidspec);
        session.setTransferMode(ssidsr.getReversed());

        /* SSID response information will be kept on Initiator session object */
        session.setResponseUser(ssidcode);
        session.setResponseUserData(ssiduser);

    }

    protected void checkSessionParamsViolation(int ssidsdeb, int ssidcred, TransferMode ssidsr, boolean ssidspec,
            OdetteFtpSession session) throws OdetteFtpException {

        String err;

        /* Check Data Exchange Buffer size */
        if ((ssidsdeb < MIN_OEB_LENGTH) || (ssidsdeb > MAX_OEB_LENGTH)) {
            err = "Illegal Data Exchange Buffer size: " + ssidsdeb;
            LOGGER.error("[{}] Session setup failed. {}", session, err);
            abnormalRelease(session, EndSessionReason.INVALID_COMMAND_DATA, err);
        }

        /* Check Window Credit size */
        if ((ssidcred < 1) || (ssidcred > 1000)) {
            err = "Illegal Window Credit size: " + ssidcred;
            LOGGER.error("[{}] Session setup failed. {}", session, err);
            abnormalRelease(session, EndSessionReason.INVALID_COMMAND_DATA, err);
        }

        /* Assure that odette-ftp peers have compatible transfer modes. */
        TransferMode localMode = session.getTransferMode();
        if ((ssidsr != null) && (localMode != TransferMode.BOTH) && (ssidsr == localMode)) {
            err = "Invalid transfer mode: " + ssidsr;
            LOGGER.error("[{}] Session setup failed. {}", session, err);
            abnormalRelease(session, EndSessionReason.INCOMPATIBLE_MODE, err);
        }

//        /*
//         * FALSE is the only is valid value for TCP. The Special Logic
//         * extensions are only useful in an X.25 environment and are not
//         * supported for TCP/IP.
//         */
//        if (ssidspec) {
//            err = "Special Logic is not supported.";
//            LOGGER.error("[{}] Session setup failed. {}", session, err);
//            abnormalRelease(session, EndSessionReason.INCOMPATIBLE_MODE, err);
//        }

    }

    protected void responderSendStartSession(OdetteFtpSession session, CommandExchangeBuffer ssid)
            throws OdetteFtpException {

        /* Get parameters from Start Session command */
        int ssidlev = Integer.parseInt(ssid.getStringAttribute(SSIDLEV_FIELD));
        String ssidcode = ssid.getStringAttribute(SSIDCODE_FIELD);
        String ssidpswd = ssid.getStringAttribute(SSIDPSWD_FIELD);
        int ssidsdeb = Integer.parseInt(ssid.getStringAttribute(SSIDSDEB_FIELD));
        TransferMode ssidsr = TransferMode.parse(ssid.getStringAttribute(SSIDSR_FIELD));
        boolean ssidcmpr = valueOfYesNo(ssid.getStringAttribute(SSIDCMPR_FIELD));
        boolean ssidrest = valueOfYesNo(ssid.getStringAttribute(SSIDREST_FIELD));
        boolean ssidspec = valueOfYesNo(ssid.getStringAttribute(SSIDSPEC_FIELD));
        int ssidcred = Integer.parseInt(ssid.getStringAttribute(SSIDCRED_FIELD));
//        String ssiduser = ssid.getStringAttribute(SSIDUSER_FIELD);

        /* Procedure to perform parameters validity checking */
        checkSessionParamsViolation(ssidsdeb, ssidcred, ssidsr, ssidspec, session);

        /*
         * Perform various protocol handshaking
         * --------------------------------------------------------------------
         */

        Oftplet oftplet = getSessionOftplet(session);

        OdetteFtpVersion version = OdetteFtpVersion.parse(ssidlev);
        if (version != session.getVersion()) {
            if (oftplet.isProtocolVersionSupported(version)) {
            	//
                // agree to use another supported odette-ftp protocol version
            	// in this session
            	//
                session.setVersion(version);
            }
        }

        boolean specialLogic = false;
        int dataBufferSize = Math.min(ssidsdeb, session.getDataBufferSize());
        int windowSize = Math.min(ssidcred, session.getWindowSize());
        boolean compression = (ssidcmpr && session.isCompressionSupported());
        boolean restart = (ssidrest && session.isRestartSupported());

        TransferMode remoteModeReversed = ssidsr.getReversed();
		session.setTransferMode(remoteModeReversed);

        session.setDataBufferSize(dataBufferSize);
        session.setWindowSize(windowSize);
        session.setCompressionSupport(compression);
        session.setRestartSupport(restart);
        session.setSpecialLogic(specialLogic);

        /*
         * Responder answer a Start Session with identification params back to
         * the Initiator. Use callback retrieval.
         * --------------------------------------------------------------------
         */

        String responseUser = ssidcode;
        String responsePswd = ssidpswd;

        PasswordCallback pwdCallback = new PasswordCallback(ssidcode, ssidpswd);
        if (handleCallback(session, pwdCallback)) {
            responseUser = pwdCallback.getUsername();
            responsePswd = pwdCallback.getPassword();
        }

        session.setResponseUser(responseUser);

        /*
         * Use implementation version specific buildStartSession from session
         * preferences to send the response SSID back to the peer.
         */
        CommandExchangeBuffer responseSsid = buildStartSessionCommand(responseUser, responsePswd,
                session.getResponseUserData(), session);
        session.write(responseSsid);

    }

    // ABSTRACT METHODS
    // -------------------------------------------------------------------------

    protected abstract CommandExchangeBuffer buildStartFileNegativeAnswerCommand(AnswerReason reason, String reasonText,
            boolean retryLater);

    protected abstract CommandExchangeBuffer buildEndFileNegativeAnswerCommand(AnswerReason reason, String reasonText);

    protected abstract CommandExchangeBuffer buildStartFilePositiveAnswerCommand(long answerCount);

    protected abstract CommandExchangeBuffer buildStartFileCommand(OdetteFtpSession session, VirtualFile vf);

    protected abstract CommandExchangeBuffer buildEndFilePositiveAnswerCommand(boolean changeDirection);

    protected abstract CommandExchangeBuffer buildEndFileCommand(long recordCount, long unitCount);

    protected abstract CommandExchangeBuffer buildStartSessionCommand(String code, String pswd, String userData, OdetteFtpSession session);

    protected abstract CommandExchangeBuffer buildEndSessionCommand(EndSessionReason reason, String reasonText);

    protected abstract CommandExchangeBuffer buildDeliveryNotificationCommand(DeliveryNotification notif);

    protected abstract CommandExchangeBuffer buildReadyToReceiveCommand();

    protected abstract DefaultVirtualFile buildVirtualFileObject(OdetteFtpSession session, CommandExchangeBuffer sfid) throws OdetteFtpException;

    protected abstract DeliveryNotification buildEndToEndResponse(CommandExchangeBuffer eerp);

    protected abstract DeliveryNotification buildNegativeEndResponse(CommandExchangeBuffer nerp) throws OdetteFtpException;

    protected abstract AnswerReasonInfo buildAnswerReasonInfoObject(CommandExchangeBuffer sfna) throws OdetteFtpException;

    protected abstract EndSessionReasonInfo buildEndSessionReasonInfoObject(CommandExchangeBuffer esid) throws OdetteFtpException;


    protected void release(OdetteFtpSession session) throws OdetteFtpException {
        protocolRelease(session, EndSessionReason.NORMAL_TERMINATION, null);
    }

    protected abstract long protocolMaxFileSizeSupported();

    // Implementation specific methods
    // -------------------------------------------------------------------------

    protected void speakerChangeDirection(final OdetteFtpSession session) throws OdetteFtpException {

        // send Change Direction command
        CommandExchangeBuffer cd = CommandBuilder.changeDirection();
        session.write(cd, new Runnable() {
            // on completation do
            public void run() {

                // Change odette-ftp entity state in session context.
                session.changeState();

                LOGGER.debug("[{}] CD sent. Odette FTP entity state changed: SPEAKER --> LISTENER", session);

            }
        });

    }

    protected boolean startSessionPasswordAuthentication(OdetteFtpSession session, CommandExchangeBuffer ssid, boolean mandatory) throws OdetteFtpException {

        String ssidcode = ssid.getStringAttribute(SSIDCODE_FIELD);
        String ssidpswd = ssid.getStringAttribute(SSIDPSWD_FIELD);

        LOGGER.debug("[{}] SSID received. Performing remote peer authentication (ssidcode: {}).", session, ssidcode);

        // Perform authentication using CallbackHandler provided by user
        PasswordAuthenticationCallback pwdAuthCallback = new PasswordAuthenticationCallback(ssidcode, ssidpswd);
        if (!handleCallback(session, pwdAuthCallback) && mandatory) {
            // already did logging within handleCallback() method
            abnormalRelease(session, EndSessionReason.RESOURCES_NOT_AVAIABLE,
                    "Password authentication engine not available.");
        }

        LOGGER.trace("[{}] handled authenticator callback = {}", session, pwdAuthCallback);

        if (!pwdAuthCallback.isSuccess() && mandatory) {
        	// has failed
        	EndSessionReason cause = pwdAuthCallback.getCause();
        	if (cause == null) {
        		cause = EndSessionReason.UNSPECIFIED_ABORT;
        	}

    		abort(session, cause, "Authentication error: " + cause.getDescription());
    		return false;
        }

        // authentication succeed
    	return true;

    }

	protected boolean handleCallback(OdetteFtpSession session, Callback callback) {

        Oftplet oftplet = getSessionOftplet(session);
        SecurityContext securityContext = oftplet.getSecurityContext();

        if (securityContext == null) {
            LOGGER.warn("[{}] ISecurityContext is not set on session object. Cannot handle callback: {} ", session,
                    callback);
            return false;
        }

        CallbackHandler callbackHandler = securityContext.getCallbackHandler();
        if (callbackHandler == null) {
            LOGGER.warn("[{}] A null CallbackHandler were provided by ISecurityContext. Cannot handle callback: {}",
                    session, callback);
            return false;
        }

        try {
            callbackHandler.handle(new Callback[] { callback });
        } catch (Throwable t) {
            LOGGER.trace("[" + session + "] Callback handler error (type: " + callback.getClass().getName() + ")", t);
            return false;
        }

        return true;
    }

    protected void speakerTransmitRequests(OdetteFtpSession session) throws OdetteFtpException {

        Oftplet oftplet = getSessionOftplet(session);
        OftpletSpeaker oftpletSpeaker = oftplet.getSpeaker();

        // doesn't implement the Oftplet speaker
        if (oftpletSpeaker == null) {
            LOGGER.debug("[{}] Nothing to transmit. Oftplet speaker returned null. Oftplet: {}", session, oftplet);

            // change direction or end session
            speakerChangeDirectionPreventingLoop(session);
            return;
        }

        OdetteFtpObject request = oftpletSpeaker.nextOftpObjectToSend();

        // nothing to transmit
        if (request == null) {
            LOGGER.debug("[{}] No outgoing request to transmit. Empty queue in Oftplet: {}", session, oftplet);

            // change direction or end session
            speakerChangeDirectionPreventingLoop(session);
            return;
        }
        
        OdetteFtpVersion sessionVersion = session.getVersion();

        boolean isVirtualFileRequest = (request instanceof VirtualFile);

        // when it's a send file request but sending is not supported
        if (isVirtualFileRequest && !isSendingSupported(session)) {

            LOGGER.error("[{}] Cannot transmit Virtual File in receiver-only transfer mode: {}", session, request);

            // set answer reason set to FILE_DIRECTION_REFUSED for OFTP
            // v1.4+ and UNSPECIFIED for OFTP v1.3 and earlier
            AnswerReason rejectReason = (sessionVersion.isEqualOrOlder(OFTP_V14) ?
                    AnswerReason.FILE_DIRECTION_REFUSED : AnswerReason.UNSPECIFIED);

            String rejectDesc = null;

            LOGGER.debug("[{}] Firing onTransmitRefused(). oftplet: {}, reason: {}, reasonText: {}, retryLater: true",
                    new Object[] { session, oftplet, rejectReason, rejectDesc});

            oftpletSpeaker.onSendFileError((VirtualFile) request, new AnswerReasonInfo(rejectReason, rejectDesc), true);

            // change direction or end session
            speakerChangeDirectionPreventingLoop(session);
            return;
        }

        /*
         * Transmit the outgoing request
         */
        if (isVirtualFileRequest) {

            VirtualFile normalizedVirtualFile = normalizeVirtualFile(session, (VirtualFile) request);
            speakerStartFile(session, normalizedVirtualFile);

            request = normalizedVirtualFile;

        } else {

            DeliveryNotification notif = (DeliveryNotification) request;
            speakerDeliveryNotification(session, notif);
        }

        // set request as current exchange in the session object
        setSessionCurrentRequest(session, request);

    }

    /**
     * If last command received is not CD, so turn direction. Otherwise, do a
     * normal end session termination.
     * 
     * @param session
     * @throws OdetteFtpException
     */
    protected void speakerChangeDirectionPreventingLoop(OdetteFtpSession session) throws OdetteFtpException {
        if (CommandIdentifier.CD == session.getLastCommandReceived()) {
            release(session);
        } else {
            speakerChangeDirection(session);
        }
    }

    protected final void resetOutgoingCredits(OdetteFtpSession session) {
        session.setOutgoingCredits(session.getWindowSize());
    }

    protected final void resetIncomingCredits(OdetteFtpSession session) {
        session.setIncomingCredits(session.getWindowSize());
    }

    protected final void consumeOutgoingCredits(OdetteFtpSession session) {
        session.setOutgoingCredits(session.getOutgoingCredits() - 1);
    }

    protected final void consumeIncomingCredits(OdetteFtpSession session) {
        session.setIncomingCredits(session.getIncomingCredits() - 1);
    }

    protected CommandExchangeBuffer buildSetCreditCommand() {
        return CommandBuilder.setCredit();
    }

}
