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
