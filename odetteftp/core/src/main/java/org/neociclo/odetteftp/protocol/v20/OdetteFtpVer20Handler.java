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
package org.neociclo.odetteftp.protocol.v20;

import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_RECORD_SIZE;
import static org.neociclo.odetteftp.protocol.CommandBuilder.readyToReceive;
import static org.neociclo.odetteftp.protocol.EndSessionReason.INCOMPATIBLE_SECURE_AUTHENTICATION;
import static org.neociclo.odetteftp.protocol.EndSessionReason.INVALID_CHALLENGE_RESPONSE;
import static org.neociclo.odetteftp.protocol.EndSessionReason.PROTOCOL_VIOLATION;
import static org.neociclo.odetteftp.protocol.EndSessionReason.RESOURCES_NOT_AVAIABLE;
import static org.neociclo.odetteftp.protocol.v13.CommandBuilderVer13.endFilePositiveAnswer;
import static org.neociclo.odetteftp.protocol.v20.CipherSuite.NO_CIPHER_SUITE_SELECTION;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.authenticationChallenge;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.authenticationChallengeResponse;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.endFile;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.endFileNegativeAnswer;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.endSession;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.endToEndResponse;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.negativeEndResponse;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.securityChangeDirection;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.startFile;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.startFileNegativeAnswer;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.startFilePositiveAnswer;
import static org.neociclo.odetteftp.protocol.v20.CommandBuilderVer20.startSession;
import static org.neociclo.odetteftp.protocol.v20.FileCompression.NO_COMPRESSION;
import static org.neociclo.odetteftp.protocol.v20.FileEnveloping.NO_ENVELOPE;
import static org.neociclo.odetteftp.protocol.v20.SecurityLevel.NO_SECURITY_SERVICES;
import static org.neociclo.odetteftp.util.CommandFormatConstants.AUCHCHAL_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.AURPRSP_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EERPDATE_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EERPDEST_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EERPDSN_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EERPHSH_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EERPORIG_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EERPSIG_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EERPTIME_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EERPUSER_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EFNAREAST_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EFNAREAS_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.ESIDREAST_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.ESIDREAS_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPCREA_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPDATE_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPDEST_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPDSN_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPHSH_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPORIG_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPREAST_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPREAS_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPSIG_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.NERPTIME_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDCIPH_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDCOMP_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDDATE_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDDESC_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDDEST_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDDSN_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDENV_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDFMT_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDFSIZ_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDLRECL_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDORIG_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDOSIZ_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDREST_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDSEC_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDSIGN_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDTIME_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDUSER_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFNAREAST_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFNAREAS_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SSIDAUTH_FIELD;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.AUTHENTICATION_CHALLENGE_SIZE;
import static org.neociclo.odetteftp.util.ProtocolUtil.valueOfYesNo;
import static org.neociclo.odetteftp.util.SessionHelper.isInitiator;
import static org.neociclo.odetteftp.util.SessionHelper.isSessionSecureAuthenticated;
import static org.neociclo.odetteftp.util.SessionHelper.setSessionSecureAuthenticated;

import java.util.Arrays;
import java.util.Date;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.oftplet.AnswerReasonInfo;
import org.neociclo.odetteftp.oftplet.EndSessionReasonInfo;
import org.neociclo.odetteftp.protocol.AnswerReason;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.CommandIdentifier;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.DeliveryNotification.EndResponseType;
import org.neociclo.odetteftp.protocol.EndSessionReason;
import org.neociclo.odetteftp.protocol.NegativeResponseReason;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.protocol.v14.OdetteFtpVer14Handler;
import org.neociclo.odetteftp.security.AuthenticationChallengeCallback;
import org.neociclo.odetteftp.security.EncryptAuthenticationChallengeCallback;
import org.neociclo.odetteftp.util.BufferUtil;
import org.neociclo.odetteftp.util.OftpUtil;
import org.neociclo.odetteftp.util.ProtocolUtil;
import org.neociclo.odetteftp.util.TimestampTicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 */
public class OdetteFtpVer20Handler extends OdetteFtpVer14Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OdetteFtpVer20Handler.class);

    protected static final long MAX_TRANSMITTED_FILE_SIZE_VER20 = 9999999999999L;

    // OFTP2 Protocol implementation
    // -------------------------------------------------------------------------

    @Override
    protected void responderSendStartSession(OdetteFtpSession session, CommandExchangeBuffer ssid)
            throws OdetteFtpException {

    	if (!checkSecureAuthenticationNegotiation(session, ssid)) {
    		return;
    	}

        super.responderSendStartSession(session, ssid);
    }

	@Override
    protected void initiatorStartSessionReceived(OdetteFtpSession session, CommandExchangeBuffer ssid)
    		throws OdetteFtpException {

    	if (!checkSecureAuthenticationNegotiation(session, ssid)) {
    		return;
    	}

    	super.initiatorStartSessionReceived(session, ssid);

    }

	private boolean checkSecureAuthenticationNegotiation(OdetteFtpSession session, CommandExchangeBuffer ssid) throws OdetteFtpException {

		// Handshaking on ODETTE-FTP v2.0 secure authentication param
        boolean ssidauth = valueOfYesNo(ssid.getStringAttribute(SSIDAUTH_FIELD));
        boolean secureAuthenticationNegotiation = (ssidauth == session.useSecureAuthentication());

        // no negotiation of secure authentication is allowed
        if (!secureAuthenticationNegotiation) {
            abnormalRelease(session, INCOMPATIBLE_SECURE_AUTHENTICATION, "Incompatible secure authentication.");
            return false;
        }

        return true;
	}

    /**
     * Begin the Secure Authentication phase after having exchanged SSIDs. This
     * phase is perform when the Secure Authentication option is agreed in the
     * Start Session negotiation phase.
     * <p/>
     * First authentication message must be sent by the Initiator.
     * 
     * <pre>
     *    1. Initiator -- SECD ------------&gt; Responder   Change Direction
     *    2.          &lt;------------ AUCH --             Challenge
     *    3.          -- AURP ------------&gt;             Response
     *    4.          &lt;------------ SECD --             Change Direction
     *    5.          -- AUCH ------------&gt;             Challenge
     *    6.          &lt;------------ AURP --             Response
     * </pre>
     */
    @Override
    public void afterStartSession(OdetteFtpSession session) throws OdetteFtpException {

        if (isInitiator(session) && session.useSecureAuthentication()) {
            // transmit the Security Change Direction to begin secure auth
            CommandExchangeBuffer secd = buildSecurityChangeDirection();
            session.write(secd);
        } else {
        	super.afterStartSession(session);
        }
    }

    public void securityChangeDirectionReceived(OdetteFtpSession session, CommandExchangeBuffer command)
            throws OdetteFtpException {

        byte[] challenge = OftpUtil.generateRandomChallenge(AUTHENTICATION_CHALLENGE_SIZE);
        session.setSecureAuthenticationChallenge(challenge);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[{}] SECD received. Generated authentication challenge (plain): {}", session, BufferUtil
                    .toHexString(challenge));
        }
        
        EncryptAuthenticationChallengeCallback encryptChallengeCallback = new EncryptAuthenticationChallengeCallback(
                challenge, session);

        if (!handleCallback(session, encryptChallengeCallback)) {
            // already did logging within handleCallback() method
            abnormalRelease(session, RESOURCES_NOT_AVAIABLE,
                    "Authentication Challenge encryption/enveloping failed. Engine is not available.");
            return;
        }

        byte[] encodedChallenge = encryptChallengeCallback.getEncodedChallenge();

        if (encodedChallenge == null || encodedChallenge.length == 0) {
            String nullEncodedChallenge = "Callback returned null/empty encoded challenge.";
            LOGGER.error("[{}] SECD received. Secure Authentication failed. {}", session, nullEncodedChallenge);
            abnormalRelease(session, RESOURCES_NOT_AVAIABLE, nullEncodedChallenge);
            return;
        }

        CommandExchangeBuffer auch = buildAuthenticationChallenge(encodedChallenge);
        session.write(auch);

    }

    public void authenticationChallengeReceived(OdetteFtpSession session, CommandExchangeBuffer auch)
            throws OdetteFtpException {

        byte[] encodedChallenge = auch.getByteArrayAttribute(AUCHCHAL_FIELD);

		AuthenticationChallengeCallback challengeCallback = new AuthenticationChallengeCallback(encodedChallenge,
				session);

        if (!handleCallback(session, challengeCallback)) {
            // already did logging within handleCallback() method
            abnormalRelease(session, RESOURCES_NOT_AVAIABLE,
                    "Authentication Challenge decryption/unenveloping failed. Engine is not available.");
            return;
        }

        byte[] challengeResponse = challengeCallback.getChallenge();

        if (challengeResponse == null || challengeResponse.length == 0) {
            String nullChallengeResponse = "Callback returned null Challenge response.";
            LOGGER.error("[{}] AUCH received. Secure Authentication failed. {}", session, nullChallengeResponse);
            abnormalRelease(session, RESOURCES_NOT_AVAIABLE, nullChallengeResponse);
            return;
        }

        // send auth challenge response back
        CommandExchangeBuffer aurp = authenticationChallengeResponse(challengeResponse);
        session.write(aurp);
        
    }

    public void authenticationResponseReceived(OdetteFtpSession session, CommandExchangeBuffer aurp)
            throws OdetteFtpException {

        byte[] challengeResponse = aurp.getByteArrayAttribute(AURPRSP_FIELD);

        if (!Arrays.equals(session.getSecureAuthenticationChallenge(), challengeResponse)) {
            LOGGER.error("[{}] AURP received. Invalid challenge response: {}", session, BufferUtil
                    .toHexString(challengeResponse));
            abnormalRelease(session, INVALID_CHALLENGE_RESPONSE, "Invalid authentication challenge resposne.");
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[{}] AURP received. Received challenge response: {}", session, BufferUtil
                    .toHexString(challengeResponse));
        }

        if (isInitiator(session)) {
            // secure authentication is now completed
            setSessionSecureAuthenticated(session);
            // the spearker's voice
            speakerTransmitRequests(session);
        } else {
            // send the Security Change Direction
            CommandExchangeBuffer secd = buildSecurityChangeDirection();
            session.write(secd);
        }

    }

    // Implementation specific
    // -------------------------------------------------------------------------

    @Override
    protected AnswerReasonInfo buildAnswerReasonInfoObject(CommandExchangeBuffer response) throws OdetteFtpException {
        CommandIdentifier id = response.getIdentifier();

        switch (id) {
        case SFNA:
            AnswerReason sfnaReason = AnswerReason.parse(Integer.parseInt(response.getStringAttribute(SFNAREAS_FIELD)));
            String sfnaReasonText = response.getStringAttribute(SFNAREAST_FIELD);
            return new AnswerReasonInfo(sfnaReason, sfnaReasonText);
        case EFNA:
            AnswerReason efnaReason = AnswerReason.parse(Integer.parseInt(response.getStringAttribute(EFNAREAS_FIELD)));
            String efnaReasonText = response.getStringAttribute(EFNAREAST_FIELD);
            return new AnswerReasonInfo(efnaReason, efnaReasonText);
        default:
            return null;
        }
    }

    @Override
    protected CommandExchangeBuffer buildDeliveryNotificationCommand(DeliveryNotification notif) {

        byte[] fileHash = null;
        byte[] signature = null;

        // OFTP20 delivery notification
        if (notif instanceof SignedDeliveryNotification) {
            SignedDeliveryNotification signed = (SignedDeliveryNotification) notif;
            fileHash = signed.getVirtualFileHash();
            signature = signed.getNotificationSignature();
        }

		if (notif.getType() == EndResponseType.END_TO_END_RESPONSE) {
			return endToEndResponse(notif.getDatasetName(), notif.getDateTime(), notif.getTicker(),
					notif.getUserData(), notif.getDestination(), notif.getOriginator(), fileHash, signature);
		} else {
			return negativeEndResponse(notif.getDatasetName(), notif.getDateTime(), notif.getTicker(),
					notif.getDestination(), notif.getOriginator(), notif.getCreator(), notif.getReason(),
					notif.getReasonText(), fileHash, signature);
		}

    }

    @Override
    protected CommandExchangeBuffer buildEndFileCommand(long recordCount, long unitCount) {
        return endFile(recordCount, unitCount);
    }

    @Override
    protected CommandExchangeBuffer buildEndFileNegativeAnswerCommand(AnswerReason reason, String reasonText) {
        return endFileNegativeAnswer(reason, reasonText);
    }

    @Override
    protected CommandExchangeBuffer buildEndFilePositiveAnswerCommand(boolean changeDirection) {
        return endFilePositiveAnswer(changeDirection);
    }

    @Override
    protected CommandExchangeBuffer buildEndSessionCommand(EndSessionReason reason, String reasonText) {
        return endSession(reason, reasonText);
    }

    @Override
    protected EndSessionReasonInfo buildEndSessionReasonInfoObject(CommandExchangeBuffer esid)
            throws OdetteFtpException {
        EndSessionReason reason = EndSessionReason.parse(Integer.valueOf(esid.getStringAttribute(ESIDREAS_FIELD)));
        String reasonText = esid.getStringAttribute(ESIDREAST_FIELD);
        return new EndSessionReasonInfo(reason, reasonText);
    }

    @Override
    protected DeliveryNotification buildEndToEndResponse(CommandExchangeBuffer eerp) {

        /* Retrieve parameter values from the command exchange buffer. */
        String datasetName = eerp.getStringAttribute(EERPDSN_FIELD);
        String fileDate = eerp.getStringAttribute(EERPDATE_FIELD);
        String fileTime = eerp.getStringAttribute(EERPTIME_FIELD);
        String userData = eerp.getStringAttribute(EERPUSER_FIELD);
        String destination = eerp.getStringAttribute(EERPDEST_FIELD);
        String originator = eerp.getStringAttribute(EERPORIG_FIELD);
        byte[] fileHash = eerp.getByteArrayAttribute(EERPHSH_FIELD);
        byte[] notifSignature = eerp.getByteArrayAttribute(EERPSIG_FIELD);

        Date fileDateTime = parseDateTime(fileDate, fileTime);
        short ticker = parseTimeTicker(fileTime);

        /* Prepare the File Delivery acknowledgment data object. */
        DefaultSignedDeliveryNotification notif = new DefaultSignedDeliveryNotification(EndResponseType.END_TO_END_RESPONSE);
        notif.setDatasetName(datasetName);
        notif.setDateTime(fileDateTime);
        notif.setTicker(ticker);
        notif.setDestination(destination);
        notif.setOriginator(originator);
        notif.setUserData(userData);
        notif.setVirtualFileHash(fileHash);
        notif.setNotificationSignature(notifSignature);

        return notif;
    }

    @Override
    protected DeliveryNotification buildNegativeEndResponse(CommandExchangeBuffer nerp) throws OdetteFtpException {

        /* Retrieve parameter values from the command exchange buffer. */
        String datasetName = nerp.getStringAttribute(NERPDSN_FIELD);
        String fileDate = nerp.getStringAttribute(NERPDATE_FIELD);
        String fileTime = nerp.getStringAttribute(NERPTIME_FIELD);
        String destination = nerp.getStringAttribute(NERPDEST_FIELD);
        String originator = nerp.getStringAttribute(NERPORIG_FIELD);
        String creator = nerp.getStringAttribute(NERPCREA_FIELD);
        byte[] fileHash = nerp.getByteArrayAttribute(NERPHSH_FIELD);
        byte[] notifSignature = nerp.getByteArrayAttribute(NERPSIG_FIELD);

        // Put it as unspecified first
        NegativeResponseReason reason = null;
        try {
        	NegativeResponseReason.parse(nerp.getStringAttribute(NERPREAS_FIELD));
        } catch (OdetteFtpException e) {
        	// We could not parse it, warn the user, but don't stop the processing. Just put it as "unspecified"
        	reason = NegativeResponseReason.UNSPECIFIED_REASON;
        	LOGGER.warn("Could not parse NegativeResponseReason " + nerp.getStringAttribute(NERPREAS_FIELD) + ". Setting it as UNSPECIFIED_REASON instead.");
        }
        String reasonText = nerp.getStringAttribute(NERPREAST_FIELD);

        Date fileDateTime = parseDateTime(fileDate, fileTime);
        short ticker = parseTimeTicker(fileTime);

        /* Prepare the File Delivery acknowledgment data object. */
        DefaultSignedDeliveryNotification notif = new DefaultSignedDeliveryNotification(EndResponseType.NEGATIVE_END_RESPONSE);
        notif.setDatasetName(datasetName);
        notif.setDateTime(fileDateTime);
        notif.setTicker(ticker);
        notif.setDestination(destination);
        notif.setOriginator(originator);
        notif.setCreator(creator);
        notif.setReason(reason);
        notif.setReasonText(reasonText);
        notif.setVirtualFileHash(fileHash);
        notif.setNotificationSignature(notifSignature);

        return notif;
    }

    @Override
    protected CommandExchangeBuffer buildReadyToReceiveCommand() {
        return readyToReceive();
    }

    @Override
    protected VirtualFile normalizeVirtualFile(OdetteFtpSession session, VirtualFile vf) {

        String dsn = (vf.getDatasetName() == null ? (vf.getFile() == null ? null : vf.getFile().getName()) : vf
                .getDatasetName());
        Date dateTime = (vf.getDateTime() == null ? (vf.getFile() == null ? null
                : new Date(vf.getFile().lastModified())) : vf.getDateTime());

        if (dsn == null) {
            throw new NullPointerException("Enveloped Virtual File object has null Dataset Name");
        } else if (dateTime == null) {
            throw new NullPointerException("Enveloped Virtual File object has null Date/Time");
        }

        // set API's generated timestamp counter (ticker) if empty
        Short ticker = vf.getTicker();
        if (ticker == null) {
        	ticker = Short.valueOf((short) TimestampTicker.getInstance().incrementAndGet());
        } else {
    		if (ticker > TimestampTicker.MAX_COUNTER_VALUE) {
    			ticker = 1;
    		}
        }

        int dsnLength = ReleaseFormatVer20.SFID_V20.getField(SFIDDSN_FIELD).getSize();
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

        //
        // Default OFTP2 start file values when a simple VirtualFile object
        // used to send the payload instead of an EnvelopedVirtualFile
        //

        FileCompression compressionAlgorithm = NO_COMPRESSION;
        long originalFileSize = fileSize;
        SecurityLevel securityLevel = NO_SECURITY_SERVICES;
        CipherSuite cipherSuite = NO_CIPHER_SUITE_SELECTION;
        FileEnveloping enveloping = NO_ENVELOPE;
        boolean signedNotifRequest = false;
        String fileDescription = null;


        if (vf instanceof EnvelopedVirtualFile) {
            EnvelopedVirtualFile env = (EnvelopedVirtualFile) vf;

            compressionAlgorithm = (env.getCompressionAlgorithm() == null ? NO_COMPRESSION : env
                    .getCompressionAlgorithm());
            originalFileSize = Math.max(env.getOriginalFileSize(), fileSize);
            securityLevel = (env.getSecurityLevel() == null ? NO_SECURITY_SERVICES : env.getSecurityLevel());
            cipherSuite = (env.getCipherSuite() == null ? NO_CIPHER_SUITE_SELECTION : env.getCipherSuite());
            enveloping = (env.getEnvelopingFormat() == null ? NO_ENVELOPE : env.getEnvelopingFormat());
            signedNotifRequest = env.isSignedNotificationRequest();
            fileDescription = env.getFileDescription();

        }

        // return the normalized virtual file
        DefaultNormalizedEnvelopedVirtualFile n = new DefaultNormalizedEnvelopedVirtualFile(vf);
        n.setDatasetName(dsn);
        n.setDateTime(dateTime);
        n.setTicker(ticker);
        n.setOriginator(orig);
        n.setDestination(dest);
        n.setRecordFormat(recordFormat);
        n.setRecordSize(recordSize);
        n.setSize(fileSize);
        n.setRestartOffset(restartOffset);

        n.setFile(vf.getFile());

        n.setEnvelopingFormat(enveloping);
        n.setCipherSuite(cipherSuite);
        n.setSecurityLevel(securityLevel);
        n.setOriginalFileSize(originalFileSize);
        n.setCompressionAlgorithm(compressionAlgorithm);

        n.setSignedNotificationRequest(signedNotifRequest);
        n.setFileDescription(fileDescription);

        return n;

    }

    @Override
    protected CommandExchangeBuffer buildStartFileCommand(OdetteFtpSession session, VirtualFile vf) {

        EnvelopedVirtualFile env = (EnvelopedVirtualFile) vf;

        return startFile(env.getDatasetName(), env.getDateTime(), env.getTicker(), env.getUserData(), env.getDestination(), env
                .getOriginator(), env.getRecordFormat(), env.getRecordSize(), env.getSize(), env.getOriginalFileSize(),
                env.getRestartOffset(), env.getSecurityLevel(), env.getCipherSuite(), env.getCompressionAlgorithm(),
                env.getEnvelopingFormat(), env.isSignedNotificationRequest(), env.getFileDescription());

    }

    @Override
    protected CommandExchangeBuffer buildStartFileNegativeAnswerCommand(AnswerReason reason, String reasonText,
            boolean retryLater) {
        return startFileNegativeAnswer(reason, reasonText, retryLater);
    }

    @Override
    protected CommandExchangeBuffer buildStartFilePositiveAnswerCommand(long answerCount) {
        return startFilePositiveAnswer(answerCount);
    }

    @Override
    protected CommandExchangeBuffer buildStartSessionCommand(String code, String pswd, String userData, OdetteFtpSession session) {

        OdetteFtpVersion version = session.getVersion();

		CommandExchangeBuffer ssid = startSession(version.getProtocolLevel(), code, pswd, session.getDataBufferSize(),
				session.getTransferMode(), session.isCompressionSupported(), session.isRestartSupported(),
				session.hasSpecialLogic(), session.getWindowSize(), session.useSecureAuthentication(), userData);
        return ssid;
    }

    @Override
    protected DefaultEnvelopedVirtualFile buildVirtualFileObject(OdetteFtpSession session, CommandExchangeBuffer sfid) throws OdetteFtpException {

        /* Read parameter values from the command exchange buffer */
        String datasetName = sfid.getStringAttribute(SFIDDSN_FIELD);
        String fileDate = sfid.getStringAttribute(SFIDDATE_FIELD);
        String fileTime = sfid.getStringAttribute(SFIDTIME_FIELD);
        String userData = sfid.getStringAttribute(SFIDUSER_FIELD);
        String destination = sfid.getStringAttribute(SFIDDEST_FIELD);
        String originator = sfid.getStringAttribute(SFIDORIG_FIELD);
        RecordFormat format = RecordFormat.parse(sfid.getStringAttribute(SFIDFMT_FIELD));
        int maxRecordSize = Integer.parseInt(sfid.getStringAttribute(SFIDLRECL_FIELD));
        long fileSizeBlocks = Long.parseLong(sfid.getStringAttribute(SFIDFSIZ_FIELD));
        long originalSizeBlocks = Long.parseLong(sfid.getStringAttribute(SFIDOSIZ_FIELD));
        long restartPosition = Long.parseLong(sfid.getStringAttribute(SFIDREST_FIELD));
        SecurityLevel securityLevel = SecurityLevel.parse(Integer.parseInt(sfid.getStringAttribute(SFIDSEC_FIELD)));
        CipherSuite cipherSuite = CipherSuite.parse(Integer.parseInt(sfid.getStringAttribute(SFIDCIPH_FIELD)));
        FileCompression compressionAlgorithm = FileCompression.parse(Integer.parseInt(sfid.getStringAttribute(SFIDCOMP_FIELD)));
        FileEnveloping envelopeFormat = FileEnveloping.parse(Integer.parseInt(sfid.getStringAttribute(SFIDENV_FIELD)));
        boolean signedNotif = valueOfYesNo(sfid.getStringAttribute(SFIDSIGN_FIELD));
        String fileDescription = sfid.getStringAttribute(SFIDDESC_FIELD);

        Date fileDateTime = parseDateTime(fileDate, fileTime);
        short ticker = parseTimeTicker(fileTime);
        
        DefaultEnvelopedVirtualFile vf = new DefaultEnvelopedVirtualFile();
        vf.setDatasetName(datasetName);
        vf.setDateTime(fileDateTime);
        vf.setTicker(ticker);
        vf.setDestination(destination);
        vf.setSize(fileSizeBlocks);
        vf.setOriginator(originator);
        vf.setRecordFormat(format);
        vf.setRecordSize(maxRecordSize);
        vf.setRestartOffset(restartPosition);
        vf.setUserData(userData);

        vf.setOriginalFileSize(originalSizeBlocks);
        vf.setSecurityLevel(securityLevel);
        vf.setCipherSuite(cipherSuite);
        vf.setCompressionAlgorithm(compressionAlgorithm);
        vf.setEnvelopingFormat(envelopeFormat);
        vf.setSignedNotificationRequest(signedNotif);
        vf.setFileDescription(fileDescription);

        return vf;

    }

    @Override
    protected long protocolMaxFileSizeSupported() {
        return MAX_TRANSMITTED_FILE_SIZE_VER20;
    }

    protected void checkSecureAuthentication(OdetteFtpSession session) throws OdetteFtpException {
        if (session.useSecureAuthentication() && !isSessionSecureAuthenticated(session)) {
            abnormalRelease(session, PROTOCOL_VIOLATION, "Secure Authentication required in this session");
            return;
        }
    }
    protected CommandExchangeBuffer buildAuthenticationChallenge(byte[] encodedChallenge) {
        return authenticationChallenge(encodedChallenge);
    }

    protected CommandExchangeBuffer buildSecurityChangeDirection() {
        return securityChangeDirection();
    }

}
