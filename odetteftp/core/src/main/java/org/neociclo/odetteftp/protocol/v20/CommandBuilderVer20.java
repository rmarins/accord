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
package org.neociclo.odetteftp.protocol.v20;

import static org.neociclo.odetteftp.util.CommandFormatConstants.*;

import static org.neociclo.odetteftp.protocol.CommandIdentifier.*;
import static org.neociclo.odetteftp.protocol.v20.ReleaseFormatVer20.*;
import static org.neociclo.odetteftp.util.ProtocolUtil.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.protocol.AnswerReason;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.EndSessionReason;
import org.neociclo.odetteftp.protocol.NegativeResponseReason;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.protocol.v14.CommandBuilderVer14;
import org.neociclo.odetteftp.util.ProtocolUtil;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class CommandBuilderVer20 extends CommandBuilderVer14 {

    /**
     * String formatter pattern for converting a <code>Date</code> value to the
     * Date stamp used on commands.
     * 
     * @see SimpleDateFormat#SimpleDateFormat(java.lang.String)
     */
    public static final String DATE_STAMP_PATTERN = "yyyyMMdd";

    /**
     * String formatter pattern for converting a <code>Date</code> value to the
     * Time stamp used on commands.
     * 
     * @see SimpleDateFormat#SimpleDateFormat(java.lang.String)
     */
    public static final String TIME_STAMP_PATTERN = "HHmmss";

    public static final int MAX_FILE_DESCRIPTION_LENGTH = 999;

    public static final int MAX_REASON_TEXT_LENGTH = 999;

    public static CommandExchangeBuffer startSession(int protocolLevel, String code, String pswd, int sdeb, TransferMode mode,
            boolean compression, boolean restart, boolean specialLogic, int credit, boolean authentication,
            String userData) {

        CommandExchangeBuffer ssid = new CommandExchangeBuffer(SSID_V20);

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
        ssid.setAttribute(SSIDAUTH_FIELD, yesNo(authentication));
        ssid.setAttribute(SSIDUSER_FIELD, userData);
        ssid.setAttribute(SSIDCR_FIELD, PROTOCOL_CARRIAGE_RETURN);

        return ssid;

    }

    public static CommandExchangeBuffer endSession(EndSessionReason reason, String reasonText) {

        CommandExchangeBuffer esid = new CommandExchangeBuffer(ESID_V20);

        esid.setAttribute(ESIDCMD_FIELD, String.valueOf(ESID.getCode()));
        esid.setAttribute(ESIDREAS_FIELD, String.valueOf(reason.getCode()));

        if (isEmpty(reasonText)) {
            // no reason text
            esid.setAttribute(ESIDREASL_FIELD, ZERO);
        } else {
            // set virtual file description
            if (reasonText.length() > MAX_REASON_TEXT_LENGTH)
                reasonText = reasonText.substring(0, MAX_REASON_TEXT_LENGTH);
            esid.setAttribute(ESIDREASL_FIELD, String.valueOf(reasonText.length()));
            esid.setAttribute(ESIDREAST_FIELD, reasonText);
        }

        esid.setAttribute(ESIDCR_FIELD, PROTOCOL_CARRIAGE_RETURN);

        return esid;
    }

    public static CommandExchangeBuffer startFile(String dsn, Date dateTime, short ticker, String userData, String destination,
            String originator, RecordFormat recordFormat, int recordSize, long fileSize, long originalFileSize,
            long offset, SecurityLevel sec, CipherSuite cipherSuite, FileCompression compressionAlgorithm,
            FileEnveloping envelopingFormat, boolean signedAck, String fileDescription) {

        CommandExchangeBuffer sfid = new CommandExchangeBuffer(SFID_V20);

		String timeWithCounter = formatTime(dateTime) + padd(Short.toString(ticker), 4, true, '0');

        sfid.setAttribute(SFIDCMD_FIELD, String.valueOf(SFID.getCode()));
        sfid.setAttribute(SFIDDSN_FIELD, dsn);
        sfid.setAttribute(SFIDDATE_FIELD, formatDate(dateTime));
        sfid.setAttribute(SFIDTIME_FIELD, timeWithCounter);
        sfid.setAttribute(SFIDUSER_FIELD, userData);
        sfid.setAttribute(SFIDDEST_FIELD, destination);
        sfid.setAttribute(SFIDORIG_FIELD, originator);
        sfid.setAttribute(SFIDFMT_FIELD, recordFormat.getCode());
        sfid.setAttribute(SFIDLRECL_FIELD, String.valueOf(recordSize));
        sfid.setAttribute(SFIDFSIZ_FIELD, String.valueOf(fileSize));
        sfid.setAttribute(SFIDOSIZ_FIELD, String.valueOf(originalFileSize));
        sfid.setAttribute(SFIDREST_FIELD, String.valueOf(offset));
        sfid.setAttribute(SFIDSEC_FIELD, String.valueOf(sec.getCode()));
        sfid.setAttribute(SFIDCIPH_FIELD, String.valueOf(cipherSuite.getCode()));
        sfid.setAttribute(SFIDCOMP_FIELD, String.valueOf(compressionAlgorithm.getCode()));
        sfid.setAttribute(SFIDENV_FIELD, String.valueOf(envelopingFormat.getCode()));
        sfid.setAttribute(SFIDSIGN_FIELD, yesNo(signedAck));

        if (isEmpty(fileDescription)) {
            // no virtual file description
            sfid.setAttribute(SFIDDESCL_FIELD, ZERO);
        } else {
            // set virtual file description
            if (fileDescription.length() > MAX_FILE_DESCRIPTION_LENGTH)
                fileDescription = fileDescription.substring(0, MAX_FILE_DESCRIPTION_LENGTH);
            sfid.setAttribute(SFIDDESCL_FIELD, String.valueOf(fileDescription.length()));
            sfid.setAttribute(SFIDDESC_FIELD, fileDescription);
        }

        return sfid;
    }

    public static String formatTime(Date dateTime) {
        return ProtocolUtil.formatDate(TIME_STAMP_PATTERN, dateTime);
    }

    public static String formatDate(Date dateTime) {
        return ProtocolUtil.formatDate(DATE_STAMP_PATTERN, dateTime);
    }

    public static CommandExchangeBuffer securityChangeDirection() {
        CommandExchangeBuffer secd = new CommandExchangeBuffer(SECD_V20);
        secd.setAttribute(SECDCMD_FIELD, String.valueOf(SECD.getCode()));
        return secd;
    }

    public static CommandExchangeBuffer authenticationChallengeResponse(byte[] challenge) {
        CommandExchangeBuffer aurp = new CommandExchangeBuffer(AURP_V20);
        aurp.setAttribute(AURPCMD_FIELD, String.valueOf(AURP.getCode()));
        aurp.setAttribute(AURPRSP_FIELD, challenge);
        return aurp;
    }

    public static CommandExchangeBuffer authenticationChallenge(byte[] encodedChallenge) {
        CommandExchangeBuffer auch = new CommandExchangeBuffer(AUCH_V20);
        auch.setAttribute(AUCHCMD_FIELD, String.valueOf(AUCH.getCode()));
        auch.setAttribute(AUCHCHLL_FIELD, formatBinaryNumber(encodedChallenge.length, AUCH_V20.getField(AUCHCHLL_FIELD)
                .getSize()));
        auch.setAttribute(AUCHCHAL_FIELD, encodedChallenge);

        return auch;
    }

    public static CommandExchangeBuffer endToEndResponse(String dsn, Date dateTime, short ticker, String userData,
            String destination, String originator, byte[] fileHash, byte[] signature) {

        CommandExchangeBuffer eerp = new CommandExchangeBuffer(EERP_V20);

		String timeWithCounter = formatTime(dateTime) + padd(Short.toString(ticker), 4, true, '0');

        eerp.setAttribute(EERPCMD_FIELD, String.valueOf(EERP.getCode()));
        eerp.setAttribute(EERPDSN_FIELD, dsn);
        eerp.setAttribute(EERPDATE_FIELD, formatDate(dateTime));
        eerp.setAttribute(EERPTIME_FIELD, timeWithCounter);
        eerp.setAttribute(EERPUSER_FIELD, userData);
        eerp.setAttribute(EERPDEST_FIELD, destination);
        eerp.setAttribute(EERPORIG_FIELD, originator);

        if (fileHash != null) {
            eerp.setAttribute(EERPHSHL_FIELD, formatBinaryNumber(fileHash.length, EERP_V20.getField(EERPHSHL_FIELD).getSize()));
            eerp.setAttribute(EERPHSH_FIELD, fileHash);
        } else { // Page 60 of RFC5024 : A binary value of 0 indicates that no hash is present. This is always the case if the EERP is not signed
			eerp.setAttribute(EERPHSHL_FIELD, formatBinaryNumber(0, EERP_V20.getField(EERPHSHL_FIELD).getSize()));
		}

        if (signature != null) {
            eerp.setAttribute(EERPSIGL_FIELD, formatBinaryNumber(signature.length,EERP_V20.getField(EERPSIGL_FIELD).getSize()));
            eerp.setAttribute(EERPSIG_FIELD, signature);
        } else {
            // Page 60 - 0 indicates the EERP is not signed
        	eerp.setAttribute(EERPSIGL_FIELD, formatBinaryNumber(0, EERP_V20.getField(EERPSIGL_FIELD).getSize()));
        }

        return eerp;
    }

	public static CommandExchangeBuffer negativeEndResponse(String dataSetName, Date dateTime, short ticker,
			String destination, String originator, String creator, NegativeResponseReason reason, String reasonText,
			byte[] fileHash, byte[] signature) {

        CommandExchangeBuffer nerp = new CommandExchangeBuffer(NERP_V20);

		String timeWithCounter = formatTime(dateTime) + padd(Short.toString(ticker), 4, true, '0');

        nerp.setAttribute(NERPCMD_FIELD, String.valueOf(NERP.getCode()));
        nerp.setAttribute(NERPDSN_FIELD, dataSetName);
        nerp.setAttribute(NERPDATE_FIELD, formatDate(dateTime));
        nerp.setAttribute(NERPTIME_FIELD, timeWithCounter);
        nerp.setAttribute(NERPDEST_FIELD, destination);
        nerp.setAttribute(NERPORIG_FIELD, originator);
        nerp.setAttribute(NERPCREA_FIELD, creator);
        nerp.setAttribute(NERPREAS_FIELD, reason.getCode());

        if (isEmpty(reasonText)) {
            // no reason text
            nerp.setAttribute(NERPREASL_FIELD, ZERO);
        } else {
            // set virtual file description
            if (reasonText.length() > MAX_REASON_TEXT_LENGTH)
                reasonText = reasonText.substring(0, MAX_REASON_TEXT_LENGTH);
            nerp.setAttribute(NERPREASL_FIELD, String.valueOf(reasonText.length()));
            nerp.setAttribute(NERPREAST_FIELD, reasonText);
        }

        if (fileHash != null) {
            nerp.setAttribute(NERPHSHL_FIELD, formatBinaryNumber(fileHash.length, NERP_V20.getField(NERPHSHL_FIELD).getSize()));
            nerp.setAttribute(NERPHSH_FIELD, fileHash);
        }

        if (signature != null) {
            nerp.setAttribute(NERPSIGL_FIELD, formatBinaryNumber(signature.length, NERP_V20.getField(NERPSIGL_FIELD)
                    .getSize()));
            nerp.setAttribute(NERPSIG_FIELD, signature);
        }

        return nerp;
    }

    public static CommandExchangeBuffer endFile(long recordCount, long unitCount) {

        CommandExchangeBuffer efid = new CommandExchangeBuffer(EFID_V20);

        efid.setAttribute(EFIDCMD_FIELD, String.valueOf(EFID.getCode()));
        efid.setAttribute(EFIDRCNT_FIELD, String.valueOf(recordCount));
        efid.setAttribute(EFIDUCNT_FIELD, String.valueOf(unitCount));

        return efid;
    }

    public static CommandExchangeBuffer endFileNegativeAnswer(AnswerReason reason, String reasonText) {

        CommandExchangeBuffer efna = new CommandExchangeBuffer(EFNA_V20);

        efna.setAttribute(EFNACMD_FIELD, String.valueOf(EFNA.getCode()));
        efna.setAttribute(EFNAREAS_FIELD, String.valueOf(reason.getCode()));

        if (isEmpty(reasonText)) {
            // no reason text
            efna.setAttribute(EFNAREASL_FIELD, ZERO);
        } else {
            // set virtual file description
            if (reasonText.length() > MAX_REASON_TEXT_LENGTH)
                reasonText = reasonText.substring(0, MAX_REASON_TEXT_LENGTH);
            efna.setAttribute(EFNAREASL_FIELD, String.valueOf(reasonText.length()));
            efna.setAttribute(EFNAREAST_FIELD, reasonText);
        }

        return efna;
    }

    /**
     * Create the Start File Positive Answer command. The only parameter
     * indicate which position the Listener agree to restart the receive of a
     * previous Virtual File.<br>
     * 
     * @param answerCount
     *            <code>int</code> lower or equal to restart count specified by
     *            the Speaker in the Start File (SFID) command. If restart
     *            facilities are not avaiable, a count of zero must be
     *            specified.
     * @return The Start File Positive Answer command with the corresponding
     *         values.
     * @throws OdetteFtpException
     */
    public static CommandExchangeBuffer startFilePositiveAnswer(long answerCount) {

        CommandExchangeBuffer sfpa = new CommandExchangeBuffer(SFPA_V20);

        sfpa.setAttribute(SFPACMD_FIELD, String.valueOf(SFPA.getCode()));
        sfpa.setAttribute(SFPAACNT_FIELD, String.valueOf(answerCount));

        return sfpa;
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
     *            Answer Reason.
     * @param reasonText
     * @param retry
     *            <code>true</code> if the transmission may be retried latter,
     *            or <code>false</code> to don't retry again.
     * @return The Start File Negative Answer command with the corresponding
     *         values.
     * @throws OdetteFtpException
     */
    public static CommandExchangeBuffer startFileNegativeAnswer(AnswerReason reason, String reasonText, boolean retry) {

        CommandExchangeBuffer sfna = new CommandExchangeBuffer(SFNA_V20);

        sfna.setAttribute(SFNACMD_FIELD, String.valueOf(SFNA.getCode()));
        sfna.setAttribute(SFNAREAS_FIELD, String.valueOf(reason.getCode()));
        sfna.setAttribute(SFNARRTR_FIELD, yesNo(retry));

        if (isEmpty(reasonText)) {
            // no virtual file description
            sfna.setAttribute(SFNAREASL_FIELD, ZERO);
        } else {
            // set virtual file description
            if (reasonText.length() > MAX_REASON_TEXT_LENGTH)
                reasonText = reasonText.substring(0, MAX_REASON_TEXT_LENGTH);
            sfna.setAttribute(SFNAREASL_FIELD, String.valueOf(reasonText.length()));
            sfna.setAttribute(SFNAREAST_FIELD, reasonText);
        }

        return sfna;
    }

}
