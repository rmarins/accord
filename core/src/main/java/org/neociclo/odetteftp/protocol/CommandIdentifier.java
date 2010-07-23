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
 * Odette FTP entities communicate by sending and receiving messages in Exchange
 * Buffers. Each Exchange Buffer correspond to a command which is defined by the
 * Command Identifier. The identifier is verified at the first octet of an
 * Exchange Buffer that define the format of the remaing buffer.
 * <p>
 * The CommandIdentifier class provide type safe instances to distinguish every
 * Exchange Buffer over all supported commands types.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public enum CommandIdentifier {

    /**
     * Authentication Challenge
     */
    AUCH('A'),

    /**
     * Authentication Response
     */
    AURP('S'),

    /**
     * Change Direction
     */
    CD('R'),

    /**
     * Set Credit
     */
    CDT('C'),

    /**
     * Data
     */
    DATA('D'),

    /**
     * End to End Response
     */
    EERP('E'),

    /**
     * End File
     */
    EFID('T'),

    /**
     * End File Negative Answer
     */
    EFNA('5'),

    /**
     * End File Positive Answer
     */
    EFPA('4'),

    /**
     * End Session
     */
    ESID('F'),

    /**
     * Negative End to End Response
     */
    NERP('N'),

    /**
     * Ready To Receive
     */
    RTR('P'),

    /**
     * Security Change Direction
     */
    SECD('J'),

    /**
     * Start File
     */
    SFID('H'),

    /**
     * Start File Negative Answer
     */
    SFNA('3'),

    /**
     * Start File Positive Answer
     */
    SFPA('2'),

    /**
     * Start Session
     */
    SSID('X'),

    /**
     * Start Session Ready Message
     */
    SSRM('I');

    /**
     * Convenient method for parsing the proper CommandIdentifier instance given
     * a identifier character.
     * 
     * @param identifier
     *        The command identifier being evaluated
     * @return CommandIdentifier Instance that correspond to the parameter
     * @throws CommandNotRecognisedException
     *         Command not recognised
     */
    public static CommandIdentifier parse(char code) throws OdetteFtpException {

        CommandIdentifier found = null;

        for (CommandIdentifier ci : CommandIdentifier.values()) {
            if (ci.getCode() == code) {
                found = ci;
                break;
            }
        }

        if (found == null) {
            throw new CommandNotRecognisedException(code, "Command not recognised: " + code + " ("
                    + Integer.toHexString((int) code).toUpperCase() + ")");
        }

        return found;
    }

    /**
     * Enum command identifier code.
     */
    private char code;

    /**
     * Private constructor to evaluate the respective command identifier
     * character regarding to the protocol specification.
     * 
     * @param aCode
     *        one valid command identifier character
     */
    private CommandIdentifier(char aCode) {
        this.code = aCode;
    }

    /**
     * Return the protocol representation of CommandIdentifier enum.
     * 
     * @return <code>String</code> corresponding protocol code.
     */
    public char getCode() {
        return code;
    }

}
