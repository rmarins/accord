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
package org.neociclo.odetteftp.protocol;

import org.neociclo.odetteftp.OdetteFtpException;

/**
 * Enumeration representing each End Session Reason available.
 * <p/>
 * Code table:
 * 
 * <pre>
 *          Value: 00 - Normal session termination
 *                 01 - Command not recognized
 *                 02 - Protocol violation
 *                 03 - User code not known
 *                 04 - Invalid password
 *                 05 - Local site emergency close down
 *                 06 - Command contained invalid data
 *                 07 - Exchange Buffer size error
 *                 08 - Resources not available
 *                 09 - Time out
 *                 10 - Mode or capabilities incompatible
 *                 99 - Unspecified Abort code
 * </pre>
 * 
 * Following End Session Reasons are supported since ODETTE FTP v2.0:
 * <p/>
 * 
 * ODETTE FTP v2.0 Extended End Session Reason Codes
 * 
 * <pre>
 *        Value: 11 - Invalid challenge response
 *               12 - Secure authentication requirements incompatible
 * </pre>
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public enum EndSessionReason {

    /**
     * Command not recognised.
     * <p>
     * An Exchange Buffer contains an invalid command identifier (1st octet of
     * the buffer).
     */
    COMMAND_NOT_RECOGNISED(1),

    /**
     * Local site emergency close down.
     * <p>
     * The local site has entered an emergency close down mode. Communications
     * are being forcibly terminated.
     */
    EMERGENCY_CLOSE_DOWN(5),

    /**
     * Exchange Buffer size error.
     * <p>
     * The length of the Exchange Buffer as determined by the Stream
     * Transmission Header is different to the length implied by the Command
     * Code.
     */
    EXCHANGE_BUFFER_SIZE_ERROR(7),

    /**
     * Mode or capabilities are incompatible.
     */
    INCOMPATIBLE_MODE(10),

    /**
     * Secure authentication requirements incompatible.
     */
    INCOMPATIBLE_SECURE_AUTHENTICATION(12),

    /**
     * Command contained invalid data.
     * <p>
     * A field within a Command Exchange buffer contains invalid data.
     */
    INVALID_COMMAND_DATA(6),

    /**
     * Invalid challenge response.
     * <p>
     * The comparison between the authentication challenge original value sent
     * (encrypted with other peer's public-key) and the returning challenge
     * response doesn't match.
     */
    INVALID_CHALLENGE_RESPONSE(11),

    /**
     * Invalid password.
     * <p>
     * A Start Session (SSID) command contains an invalid password for the
     * specified user identification.
     */
    INVALID_PASSWORD(4),

    /**
     * Normal session termination.
     */
    NORMAL_TERMINATION(0),

    /**
     * Protocol violation.
     * <p>
     * An Exchange Buffer contains an invalid command for the current state of
     * the receiver.
     */
    PROTOCOL_VIOLATION(2),

    /**
     * Resources not avaiable.
     * <p>
     * The request for connection has been denied due to a resource shortage.
     * The connection attempt should be retried later.
     */
    RESOURCES_NOT_AVAIABLE(8),

    /**
     * Time out.
     */
    TIME_OUT(9),

    /**
     * User code not known.
     * <p>
     * A Start Session (SSID) command contains an unknown or invalid
     * Identification Code.
     */
    UNKNOWN_USER_CODE(3),

    /**
     * Unspecified Abort code.
     * <p>
     * An error was detected for which no specific code is defined.
     */
    UNSPECIFIED_ABORT(99);

    /**
     * Convenient method for parsing the proper EndSessionReason enum given a
     * identifier character.
     * 
     * @param code
     *            The end session reason being evaluated
     * @return EndSessionReason enum that correspond to the given code.
     * @throws OdetteFtpException
     * @throws CommandNotRecognisedException
     *             Code not recognized
     */
    public static EndSessionReason parse(int code) throws OdetteFtpException {
        EndSessionReason found = null;

        for (EndSessionReason esr : EndSessionReason.values()) {
            if (esr.getCode() == code) {
                found = esr;
                break;
            }
        }

        if (found == null) {
            throw new CommandNotRecognisedException("End Session Reason not recognised: " + code);
        }

        return found;
    }

    private int reasonCode;

    /**
     * Enumeration constructor where reason code is specified to satisfy
     * ReasonCode interface.
     * 
     * @param aReasonCode
     */
    private EndSessionReason(int aReasonCode) {
        reasonCode = aReasonCode;
    }

    /**
     * Return the protocol representation of enum.
     * 
     * @return <code>byte</code> corresponding protocol code.
     */
    public int getCode() {
        return reasonCode;
    }
}
