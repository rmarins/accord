/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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
package org.neociclo.odetteftp.protocol.v13;

import static org.neociclo.odetteftp.util.CommandFormatConstants.*;

import static org.neociclo.odetteftp.protocol.CommandIdentifier.EERP;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EFID;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EFNA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.EFPA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.ESID;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SFID;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SFNA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SFPA;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SSID;
import static org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13.EERP_V13;
import static org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13.EFID_V13;
import static org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13.EFNA_V13;
import static org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13.EFPA_V13;
import static org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13.ESID_V13;
import static org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13.SFID_V13;
import static org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13.SFNA_V13;
import static org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13.SFPA_V13;
import static org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13.SSID_V13;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.protocol.AnswerReason;
import org.neociclo.odetteftp.protocol.CommandBuilder;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.EndSessionReason;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.util.ProtocolUtil;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class CommandBuilderVer13 extends CommandBuilder {

    /**
     * String formatter pattern for converting a <code>Date</code> value to
     * the Date stamp used on commands.
     * 
     * @see SimpleDateFormat#SimpleDateFormat(java.lang.String)
     */
    public static final String DATE_STAMP_PATTERN = "yyMMdd";

    /**
     * String formatter pattern for converting a <code>Date</code> value to
     * the Time stamp used on commands.
     * 
     * @see SimpleDateFormat#SimpleDateFormat(java.lang.String)
     */
    public static final String TIME_STAMP_PATTERN = "HHmmss";

    /**
     * Create the End File command indicating the count of records and overall
     * octets from the transmitted Virtual File.<br>
     * The count will express the real size of the file (before compression,
     * header not included). The total count is always used, even during restart
     * processing.
     * 
     * @param recordCount
     *        For FIXED or VARIABLE record formats, the exact record count. For
     *        UNSTRUCTURED or TEXTFILE, must be zero.
     * @param unitCount
     *        Exact number of units (octets) transmitted.
     * @return
     */
    public static CommandExchangeBuffer endFile(long recordCount, long unitCount) {

        CommandExchangeBuffer efid = new CommandExchangeBuffer(EFID_V13);

        efid.setAttribute(EFIDCMD_FIELD, String.valueOf(EFID.getCode()));
        efid.setAttribute(EFIDRCNT_FIELD, String.valueOf(recordCount));
        efid.setAttribute(EFIDUCNT_FIELD, String.valueOf(unitCount));

        return efid;
    }

    /**
     * Create the End File Negative Answer command providing the reason for
     * stopping transfer.
     * 
     * @param reason
     *        Reason why transmission can not proceed.
     * @return The End File Negative Answer command with the corresponding
     *         values.
     */
    public static CommandExchangeBuffer endFileNegativeAnswer(AnswerReason reason) {

        CommandExchangeBuffer efna = new CommandExchangeBuffer(EFNA_V13);

        efna.setAttribute(EFNACMD_FIELD, String.valueOf(EFNA.getCode()));
        efna.setAttribute(EFNAREAS_FIELD, String.valueOf(reason.getCode()));

        return efna;
    }

    /**
     * Create the End File Positive Answer command indicating whether the
     * Listener is requesting or not a Change Direction (CD) command from
     * Speaker or not.
     * 
     * @param changeDirection
     *        Change Direction Indicator.
     * @return The End File Positive Answer command with the corresponding
     *         values.
     */
    public static CommandExchangeBuffer endFilePositiveAnswer(boolean changeDirection) {

        CommandExchangeBuffer efpa = new CommandExchangeBuffer(EFPA_V13);

        efpa.setAttribute(EFPACMD_FIELD, String.valueOf(EFPA.getCode()));
        efpa.setAttribute(EFPACD_FIELD, yesNo(changeDirection));

        return efpa;
    }

    /**
     * Create the End Session command indicating the reason code for terminating
     * the session.
     * 
     * @param reason
     *        End Session Reason.
     * @return The End Session command with the corresponding values.
     */
    public static CommandExchangeBuffer endSession(EndSessionReason reason) {

        CommandExchangeBuffer esid = new CommandExchangeBuffer(ESID_V13);

        esid.setAttribute(ESIDCMD_FIELD, String.valueOf(ESID.getCode()));
        esid.setAttribute(ESIDREAS_FIELD, String.valueOf(reason.getCode()));
        esid.setAttribute(ESIDCR_FIELD, PROTOCOL_CARRIAGE_RETURN);

        return esid;
    }

    /**
     * Create the End to End Response command with given parameters.
     * 
     * @param dataSetName
     *        Dataset name of the Virtual File being transfered.
     * @param dateTime
     *        Virtual File date and time indicating when the file was made
     *        available for transmission.
     * @param userData
     *        May be used by the Odette FTP in any way.
     * @param destination
     *        Identification code from the Originator of the Virtual File, which
     *        created (mapped) the data for transmission.
     * @param originator
     *        Identification code of the Final Recipient of the Virtual File.
     *        This is the location that creates the EERP for the received file.
     * @return The End to End Response command with the corresponding values.
     */
    public static CommandExchangeBuffer endToEndResponse(String dataSetName, Date dateTime, String userData,
            String destination, String originator) {

        CommandExchangeBuffer eerp = new CommandExchangeBuffer(EERP_V13);

        eerp.setAttribute(EERPCMD_FIELD, String.valueOf(EERP.getCode()));
        eerp.setAttribute(EERPDSN_FIELD, dataSetName);
        eerp.setAttribute(EERPDATE_FIELD, ProtocolUtil.formatDate(DATE_STAMP_PATTERN, dateTime));
        eerp.setAttribute(EERPTIME_FIELD, ProtocolUtil.formatDate(TIME_STAMP_PATTERN, dateTime));
        eerp.setAttribute(EERPUSER_FIELD, userData);
        eerp.setAttribute(EERPDEST_FIELD, destination);
        eerp.setAttribute(EERPORIG_FIELD, originator);

        return eerp;
    }

    /**
     * Create the Start File command with given parameters.<br>
     * The Start File command includes a count allowing the restart of an
     * interrupted transmission to be negotiated. If restart facilities are not
     * available the restart count must be set to zero. The sender will start
     * with the lowest record count + 1.
     *
     * @param datasetName
     *        Dataset Name of the Virtual File being transferred assigned by
     *        bilateral agreement.
     * @param dateTime
     *        Specific Date and Time assigned by the Virtual File's Originator
     *        indicating when the file was made available for transmission.
     * @param userData
     *        May be used by the ODETTE-FTP in any way. If unused it should be
     *        initialized to spaces. It is expected that a bilateral agreement
     *        exists as to the meaning of the data.
     * @param destination
     *        The Identification Code for the final recipient of the Virtual
     *        File. This is the location that will look into the Virtual File
     *        content and perform mapping functions. It is also the location
     *        that creates the End to End Response (EERP) command for the
     *        received file.
     * @param originator
     *        The Identification Code from the Originator of the Virtual File.
     *        It is the location that created (mapped) the data for
     *        transmission.
     * @param recordFormat
     *        Virtual File format (Fixed, Variable, Unstructured, Text File).
     *        Used to calculate the restart position.
     * @param maxRecordSize
     *        Length in octets of the longest logical record which may be
     *        transferred to a location. Only user data is included. If File
     *        format is 'T' or 'U' then this attribute must be set to '00000'
     * @param fileSize
     *        File Size, 1K (1024 octets) blocks.
     * @param restartOffset
     *        Restart position.
     * @return The Start File command with the corresponding values.
     */
    public static CommandExchangeBuffer startFile(String datasetName, Date dateTime, String userData,
            String destination, String originator, RecordFormat recordFormat, int maxRecordSize, long fileSize,
            long restartOffset) {

        CommandExchangeBuffer sfid = new CommandExchangeBuffer(SFID_V13);

        sfid.setAttribute(SFIDCMD_FIELD, String.valueOf(SFID.getCode()));
        sfid.setAttribute(SFIDDSN_FIELD, datasetName);
        sfid.setAttribute(SFIDDATE_FIELD, ProtocolUtil.formatDate(DATE_STAMP_PATTERN, dateTime));
        sfid.setAttribute(SFIDTIME_FIELD, ProtocolUtil.formatDate(TIME_STAMP_PATTERN, dateTime));
        sfid.setAttribute(SFIDUSER_FIELD, userData);
        sfid.setAttribute(SFIDDEST_FIELD, destination);
        sfid.setAttribute(SFIDORIG_FIELD, originator);
        sfid.setAttribute(SFIDFMT_FIELD, recordFormat.getCode());
        sfid.setAttribute(SFIDLRECL_FIELD, String.valueOf(maxRecordSize));
        sfid.setAttribute(SFIDFSIZ_FIELD, String.valueOf(fileSize));
        sfid.setAttribute(SFIDREST_FIELD, String.valueOf(restartOffset));

        return sfid;
    }

    /**
     * Create the Start File Negative Answer command containing the the reason
     * why transmission can not proced.<br>
     * This <code>retry</code> parameter is used to advise the Speaker if it
     * should retry at a latter point in time due to a temporary condition at
     * the Listener site, such as a lack of storage space. It should be used in
     * conjunction with the Answer Reason code.
     * 
     * @param reason
     *        Answer Reason.
     * @param retry
     *        <code>true</code> if the transmission may be retried latter, or
     *        <code>false</code> to don't retry again.
     * @return The Start File Negative Answer command with the corresponding
     *         values.
     */
    public static CommandExchangeBuffer startFileNegativeAnswer(AnswerReason reason, boolean retry) {

        CommandExchangeBuffer sfna = new CommandExchangeBuffer(SFNA_V13);

        sfna.setAttribute(SFNACMD_FIELD, String.valueOf(SFNA.getCode()));
        sfna.setAttribute(SFNAREAS_FIELD, String.valueOf(reason.getCode()));
        sfna.setAttribute(SFNARRTR_FIELD, yesNo(retry));

        return sfna;
    }

    /**
     * Create the Start File Positive Answer command. The only parameter
     * indicate which position the Listener agree to restart the receive of a
     * previous Virtual File.<br>
     * 
     * @param answerCount
     *        <code>int</code> lower or equal to restart count specified by
     *        the Speaker in the Start File (SFID) command. If restart
     *        facilities are not avaiable, a count of zero must be specified.
     * @return The Start File Positive Answer command with the corresponding
     *         values.
     */
    public static CommandExchangeBuffer startFilePositiveAnswer(long answerCount) {

        CommandExchangeBuffer sfpa = new CommandExchangeBuffer(SFPA_V13);

        sfpa.setAttribute(SFPACMD_FIELD, String.valueOf(SFPA.getCode()));
        sfpa.setAttribute(SFPAACNT_FIELD, String.valueOf(answerCount));

        return sfpa;
    }

    /**
     * Create the Start Session command with given parameters. It belongs to the
     * Start Session Phase, and is performed in both direction to negotiate
     * capabilities and session wide parameters between locations.
     * @param protocolLevel protocolLevel
     * 
     * @param code
     *        Initiator's Identification Code which uniquely identifies the
     *        Initiator (sender) participating in the Odette FTP session.
     * @param pswd
     *        Key to authenticate the sender. Assigned by bilateral agreement.
     * @param sdeb
     *        The length, in octets, of the largest Exchange Buffer that can be
     *        accepted by the location.
     * @param compression
     *        Compression indicator. <code>true</code> if the location can
     *        handle compressed data. Otherwise it should be <code>false</code>.
     * @param restart
     *        Restart indication informing whether the location can handle the
     *        restart of a partially transmitted file.
     * @param specialLogic
     *        Special logic indication.
     * @param credit
     *        Credit.
     * @param userData
     *        User Data.
     * @param sendReceive
     *        Sender / Receiver capabilities:
     * @return The Start Session command with the corresponding values.
     */
    public static CommandExchangeBuffer startSession(int protocolLevel, String code, String pswd, int sdeb, TransferMode mode,
            boolean compression, boolean restart, boolean specialLogic, int credit, String userData) {

        CommandExchangeBuffer ssid = new CommandExchangeBuffer(SSID_V13);

        ssid.setAttribute(SSIDCMD_FIELD, String.valueOf(SSID.getCode()));
        ssid.setAttribute(SSIDLEV_FIELD, String.valueOf(protocolLevel));
        ssid.setAttribute(SSIDCODE_FIELD, code);
        ssid.setAttribute(SSIDPSWD_FIELD, pswd);
        ssid.setAttribute(SSIDSDEB_FIELD, String.valueOf(sdeb));
        ssid.setAttribute(SSIDSR_FIELD, String.valueOf(mode.getCode()));
        ssid.setAttribute(SSIDCMPR_FIELD, yesNo(compression));
        ssid.setAttribute(SSIDREST_FIELD, yesNo(restart));
        ssid.setAttribute(SSIDSPEC_FIELD, yesNo(specialLogic));
        ssid.setAttribute(SSIDCRED_FIELD, String.valueOf(credit));
        ssid.setAttribute(SSIDUSER_FIELD, userData);
        ssid.setAttribute(SSIDCR_FIELD, PROTOCOL_CARRIAGE_RETURN);

        return ssid;
    }

}
