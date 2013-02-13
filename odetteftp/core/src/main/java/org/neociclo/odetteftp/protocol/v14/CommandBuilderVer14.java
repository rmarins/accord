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
package org.neociclo.odetteftp.protocol.v14;

import static org.neociclo.odetteftp.util.CommandFormatConstants.*;
import static org.neociclo.odetteftp.util.ProtocolUtil.*;

import static org.neociclo.odetteftp.protocol.CommandIdentifier.EERP;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.NERP;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SFID;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SSID;
import static org.neociclo.odetteftp.protocol.v14.ReleaseFormatVer14.EERP_V14;
import static org.neociclo.odetteftp.protocol.v14.ReleaseFormatVer14.NERP_V14;
import static org.neociclo.odetteftp.protocol.v14.ReleaseFormatVer14.SFID_V14;
import static org.neociclo.odetteftp.protocol.v14.ReleaseFormatVer14.SSID_V14;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.NegativeResponseReason;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.protocol.v13.CommandBuilderVer13;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class CommandBuilderVer14 extends CommandBuilderVer13 {

    /**
     * String formatter pattern for converting a <code>Date</code> value to
     * the Date stamp used on commands.
     * 
     * @see SimpleDateFormat#SimpleDateFormat(java.lang.String)
     */
    public static final String DATE_STAMP_PATTERN = "yyyyMMdd";

    /**
     * String formatter pattern for converting a <code>Date</code> value to
     * the Time stamp used on commands.
     * 
     * @see SimpleDateFormat#SimpleDateFormat(java.lang.String)
     */
    public static final String TIME_STAMP_PATTERN = "HHmmss";

    /**
     * Create the End to End Response command with given parameters.
     * 
     * @param dataSetName
     *        Dataset Name of the Virtual File being transfered.
     * @param dateTime
     *        Virtual File date and time indicating when the file was made
     *        available for transmission.
     * @param ticker
     * @param userData
     *        May be used by the ODETTE-FTP in any way.
     * @param destination
     *        Identification code from the Originator of the Virtual File, which
     *        created (mapped) the data for transmission.
     * @param originator
     *        Identification code of the Final Recipient of the Virtual File.
     *        This is the location that creates the EERP for the received file.
     * @return The End to End Response command with the corresponding values.
     */
    public static CommandExchangeBuffer endToEndResponse(String dataSetName, Date dateTime, short ticker, String userData,
            String destination, String originator) {

        CommandExchangeBuffer eerp = new CommandExchangeBuffer(EERP_V14);

        String timeWithCounter = formatDate(TIME_STAMP_PATTERN, dateTime) + padd(Short.toString(ticker), 4, true, '0');

        eerp.setAttribute(EERPCMD_FIELD, String.valueOf(EERP.getCode()));
        eerp.setAttribute(EERPDSN_FIELD, dataSetName);
        eerp.setAttribute(EERPDATE_FIELD, formatDate(DATE_STAMP_PATTERN, dateTime));
        eerp.setAttribute(EERPTIME_FIELD, timeWithCounter);
        eerp.setAttribute(EERPUSER_FIELD, userData);
        eerp.setAttribute(EERPDEST_FIELD, destination);
        eerp.setAttribute(EERPORIG_FIELD, originator);

        return eerp;
    }

    /**
     * Create the End to End Response command with given parameters.
     * 
     * @param dataSetName
     *        Dataset name of the Virtual File being transfered.
     * @param dateTime
     *        Virtual File date and time indicating when the file was made
     *        available for transmission.
     * @param ticker
     * @param destination
     *        Identification code from the Originator of the Virtual File, which
     *        created (mapped) the data for transmission.
     * @param originator
     *        Identification code of the Final Recipient of the Virtual File.
     *        This is the location that creates the EERP for the received file.
     * @param creator
     * @param reason
     * @return The End to End Response command with the corresponding values.
     */
    public static CommandExchangeBuffer negativeEndResponse(String dataSetName, Date dateTime, short ticker, String destination,
            String originator, String creator, NegativeResponseReason reason) {

        CommandExchangeBuffer nerp = new CommandExchangeBuffer(NERP_V14);

        String timeWithCounter = formatDate(TIME_STAMP_PATTERN, dateTime) + padd(Short.toString(ticker), 4, true, '0');

        nerp.setAttribute(NERPCMD_FIELD, String.valueOf(NERP.getCode()));
        nerp.setAttribute(NERPDSN_FIELD, dataSetName);
        nerp.setAttribute(NERPDATE_FIELD, formatDate(DATE_STAMP_PATTERN, dateTime));
        nerp.setAttribute(NERPTIME_FIELD, timeWithCounter);
        nerp.setAttribute(NERPDEST_FIELD, destination);
        nerp.setAttribute(NERPORIG_FIELD, originator);
        nerp.setAttribute(NERPCREA_FIELD, creator);
        nerp.setAttribute(NERPREAS_FIELD, reason.getCode());

        return nerp;
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
     * @param ticker
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
    public static CommandExchangeBuffer startFile(String datasetName, Date dateTime, short ticker, String userData,
            String destination, String originator, RecordFormat recordFormat, int maxRecordSize, long fileSize,
            long restartOffset) {

        CommandExchangeBuffer sfid = new CommandExchangeBuffer(SFID_V14);

        String timeWithCounter = formatDate(TIME_STAMP_PATTERN, dateTime) + padd(Short.toString(ticker), 4, true, '0');

        sfid.setAttribute(SFIDCMD_FIELD, String.valueOf(SFID.getCode()));
        sfid.setAttribute(SFIDDSN_FIELD, datasetName);
        sfid.setAttribute(SFIDDATE_FIELD, formatDate(DATE_STAMP_PATTERN, dateTime));
        sfid.setAttribute(SFIDTIME_FIELD, timeWithCounter);
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
	 * Create the Start Session command with given parameters. It belongs to the
	 * Start Session Phase, and is performed in both direction to negotiate
	 * capabilities and session wide parameters between locations.
	 * 
	 * @param protocolLevel
	 *            protocol version
	 * @param code
	 *            Initiator's Identification Code which uniquely identifies the
	 *            Initiator (sender) participating in the Odette FTP session.
	 * @param pswd
	 *            Key to authenticate the sender. Assigned by bilateral
	 *            agreement.
	 * @param sdeb
	 *            The length, in octets, of the largest Exchange Buffer that can
	 *            be accepted by the location.
	 * @param compression
	 *            Compression indicator. <code>true</code> if the location can
	 *            handle compressed data. Otherwise it should be
	 *            <code>false</code>.
	 * @param restart
	 *            Restart indication informing whether the location can handle
	 *            the restart of a partially transmitted file.
	 * @param specialLogic
	 *            Special logic indication.
	 * @param credit
	 *            Credit.
	 * @param userData
	 *            User Data.
	 * @param sendReceive
	 *            Sender / Receiver capabilities:
	 * @return The Start Session command with the corresponding values.
	 */
    public static CommandExchangeBuffer startSession(int protocolLevel, String code, String pswd, int sdeb, TransferMode mode,
            boolean compression, boolean restart, boolean specialLogic, int credit, String userData) {

        CommandExchangeBuffer ssid = new CommandExchangeBuffer(SSID_V14);

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
