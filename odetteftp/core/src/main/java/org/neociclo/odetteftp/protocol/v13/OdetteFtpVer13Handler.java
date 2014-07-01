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
package org.neociclo.odetteftp.protocol.v13;

import static org.neociclo.odetteftp.protocol.CommandBuilder.readyToReceive;
import static org.neociclo.odetteftp.protocol.v13.CommandBuilderVer13.endFile;
import static org.neociclo.odetteftp.protocol.v13.CommandBuilderVer13.endFileNegativeAnswer;
import static org.neociclo.odetteftp.protocol.v13.CommandBuilderVer13.endSession;
import static org.neociclo.odetteftp.protocol.v13.CommandBuilderVer13.endToEndResponse;
import static org.neociclo.odetteftp.protocol.v13.CommandBuilderVer13.startFile;
import static org.neociclo.odetteftp.protocol.v13.CommandBuilderVer13.startFileNegativeAnswer;
import static org.neociclo.odetteftp.protocol.v13.CommandBuilderVer13.startFilePositiveAnswer;
import static org.neociclo.odetteftp.protocol.v13.CommandBuilderVer13.startSession;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EERPDATE_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EERPDEST_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EERPDSN_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EERPORIG_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EERPTIME_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EERPUSER_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.EFNAREAS_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.ESIDREAS_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDDATE_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDDEST_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDDSN_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDFMT_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDFSIZ_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDLRECL_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDORIG_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDREST_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDTIME_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFIDUSER_FIELD;
import static org.neociclo.odetteftp.util.CommandFormatConstants.SFNAREAS_FIELD;

import java.util.Date;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.oftplet.AnswerReasonInfo;
import org.neociclo.odetteftp.oftplet.EndSessionReasonInfo;
import org.neociclo.odetteftp.protocol.AnswerReason;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.CommandIdentifier;
import org.neociclo.odetteftp.protocol.DefaultDeliveryNotification;
import org.neociclo.odetteftp.protocol.DefaultHandler;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.DeliveryNotification.EndResponseType;
import org.neociclo.odetteftp.protocol.EndSessionReason;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.util.ProtocolUtil;


/**
 * @author Rafael Marins
 */
public class OdetteFtpVer13Handler extends DefaultHandler {

    /**
     * Maximum supported file size blocks in Odette FTP v1.3.
     */
    protected static final long MAX_TRANSMITTED_FILE_SIZE_VER13 = 9999999L;

    private static final String COMMAND_NOT_SUPPORTED_MSG = "Command not supported in ODETTE FTP version 1.3";

    public void authenticationChallengeReceived(OdetteFtpSession session, CommandExchangeBuffer command)
            throws OdetteFtpException {
        abnormalRelease(session, EndSessionReason.PROTOCOL_VIOLATION, COMMAND_NOT_SUPPORTED_MSG);
    }

    public void authenticationResponseReceived(OdetteFtpSession session, CommandExchangeBuffer command)
            throws OdetteFtpException {
        abnormalRelease(session, EndSessionReason.PROTOCOL_VIOLATION, COMMAND_NOT_SUPPORTED_MSG);
    }

    public void negativeEndReponseReceived(OdetteFtpSession session, CommandExchangeBuffer command) throws OdetteFtpException {
        abnormalRelease(session, EndSessionReason.PROTOCOL_VIOLATION, COMMAND_NOT_SUPPORTED_MSG);
    }

    @Override
    protected DeliveryNotification buildNegativeEndResponse(CommandExchangeBuffer nerp) throws OdetteFtpException {
        return null;
    }

    public void securityChangeDirectionReceived(OdetteFtpSession session, CommandExchangeBuffer command)
            throws OdetteFtpException {
        abnormalRelease(session, EndSessionReason.PROTOCOL_VIOLATION, COMMAND_NOT_SUPPORTED_MSG);
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    @Override
    protected long protocolMaxFileSizeSupported() {
        return MAX_TRANSMITTED_FILE_SIZE_VER13;
    }

    // PROTOCOL METHODS
    // -------------------------------------------------------------------------

    @Override
    protected CommandExchangeBuffer buildStartSessionCommand(String code, String pswd, String userData, OdetteFtpSession session) {

        OdetteFtpVersion version = session.getVersion();

        CommandExchangeBuffer ssid = startSession(version.getProtocolLevel(), code, pswd, session.getDataBufferSize(),
                session.getTransferMode(), session.isCompressionSupported(), session.isRestartSupported(), session
                        .hasSpecialLogic(), session.getWindowSize(), userData);

        return ssid;
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

        Date fileDateTime = parseDateTime(fileDate, fileTime);

        /* Prepare the File Delivery acknowledgment data object. */
        DefaultDeliveryNotification notif = new DefaultDeliveryNotification(EndResponseType.END_TO_END_RESPONSE);
        notif.setDatasetName(datasetName);
        notif.setDateTime(fileDateTime);
        notif.setDestination(destination);
        notif.setOriginator(originator);
        notif.setUserData(userData);

        return notif;
    }

    @Override
    protected CommandExchangeBuffer buildEndSessionCommand(EndSessionReason reason, String reasonText) {
        return endSession(reason);
    }

    public Date parseDateTime(String sdate, String stime) {
        // date format: yymmdd
        int year = Integer.parseInt(sdate.substring(0, 2));
        int month = Integer.parseInt(sdate.substring(2, 4));
        int day = Integer.parseInt(sdate.substring(4, 6));

        // time format: hhmmss
        int hour = Integer.parseInt(stime.substring(0, 2));
        int minute = Integer.parseInt(stime.substring(2, 4));
        int second = Integer.parseInt(stime.substring(4, 6));

        return ProtocolUtil.createDate(year, month, day, hour, minute, second, 0);
    }

    @Override
    protected CommandExchangeBuffer buildStartFileCommand(OdetteFtpSession session, VirtualFile vf) {
        return startFile(vf.getDatasetName(), vf.getDateTime(), vf.getUserData(), vf.getDestination(), vf
                .getOriginator(), vf.getRecordFormat(), vf.getRecordSize(), vf.getSize(), vf.getRestartOffset());
    }

    @Override
    protected CommandExchangeBuffer buildEndFilePositiveAnswerCommand(boolean changeDirection) {
        return CommandBuilderVer13.endFilePositiveAnswer(changeDirection);
    }

    @Override
    protected CommandExchangeBuffer buildStartFilePositiveAnswerCommand(long answerCount) {
        return startFilePositiveAnswer(answerCount);
    }

    @Override
    protected CommandExchangeBuffer buildEndFileNegativeAnswerCommand(AnswerReason reason, String reasonText) {
        return endFileNegativeAnswer(reason);
    }

    @Override
    protected CommandExchangeBuffer buildStartFileNegativeAnswerCommand(AnswerReason reason, String reasonText,
            boolean retryLater) {

        return startFileNegativeAnswer(reason, retryLater);
    }

    @Override
    protected DefaultVirtualFile buildVirtualFileObject(OdetteFtpSession session, CommandExchangeBuffer sfid) throws OdetteFtpException {

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
        long restartPosition = Long.parseLong(sfid.getStringAttribute(SFIDREST_FIELD));

        Date fileDateTime = parseDateTime(fileDate, fileTime);
        
        DefaultVirtualFile data = new DefaultVirtualFile();
        data.setDatasetName(datasetName);
        data.setDateTime(fileDateTime);
        data.setDestination(destination);
        data.setSize(fileSizeBlocks);
        data.setOriginator(originator);
        data.setRecordFormat(format);
        data.setRecordSize(maxRecordSize);
        data.setRestartOffset(restartPosition);
        data.setUserData(userData);

        return data;
    }

    @Override
    protected AnswerReasonInfo buildAnswerReasonInfoObject(CommandExchangeBuffer response) throws OdetteFtpException {

        CommandIdentifier id = response.getIdentifier();

        switch (id) {
        case SFNA:
            AnswerReason sfnaReason = AnswerReason.parse(Integer.parseInt(response.getStringAttribute(SFNAREAS_FIELD)));
            return new AnswerReasonInfo(sfnaReason);
        case EFNA:
            AnswerReason efnaReason = AnswerReason.parse(Integer.parseInt(response.getStringAttribute(EFNAREAS_FIELD)));
            return new AnswerReasonInfo(efnaReason);
        default:
            return null;
        }
    }

    @Override
    protected CommandExchangeBuffer buildEndFileCommand(long recordCount, long unitCount) {
        return endFile(recordCount, unitCount);
    }

    @Override
    protected CommandExchangeBuffer buildDeliveryNotificationCommand(DeliveryNotification notif) {
        return endToEndResponse(notif.getDatasetName(), notif.getDateTime(), notif.getUserData(), notif
                .getDestination(), notif.getOriginator());
    }

    @Override
    protected EndSessionReasonInfo buildEndSessionReasonInfoObject(CommandExchangeBuffer response) throws OdetteFtpException {
        EndSessionReason reason = EndSessionReason.parse(Integer.valueOf(response.getStringAttribute(ESIDREAS_FIELD)));
        return new EndSessionReasonInfo(reason);
    }

    @Override
    protected CommandExchangeBuffer buildReadyToReceiveCommand() {
        return readyToReceive();
    }
}
