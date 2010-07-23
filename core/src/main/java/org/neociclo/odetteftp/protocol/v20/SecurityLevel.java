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

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.protocol.CommandNotRecognisedException;

/**
 * Enumeration indicating whether the file has been signed and/or encrypted
 * before transmission.
 *
 * <pre>
 *    Value: '00'  No security services
 *           '01'  Encrypted
 *           '02'  Signed
 *           '03'  Encrypted and signed
 * </pre>
 *
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public enum SecurityLevel {

    NO_SECURITY_SERVICES(0),

    ENCRYPTED(1),

    SIGNED(2),

    ENCRYPTED_AND_SIGNED(3);

    /**
     * Convenient method for parsing the proper SecurityLevel enum given a
     * protocol code.
     * 
     * @param code
     *        The security level being evaluated
     * @return SecurityLevel enum that correspond to the given code.
     * @throws OdetteFtpException
     * @throws CommandNotRecognisedException
     *         Code not recognized
     */
    public static SecurityLevel parse(int code) throws OdetteFtpException {
        SecurityLevel found = null;

        for (SecurityLevel sl : SecurityLevel.values()) {
            if (sl.getCode() == code) {
                found = sl;
                break;
            }
        }

        if (found == null) {
            throw new CommandNotRecognisedException("Security Level not recognised: " + code);
        }

        return found;
    }

    private int levelCode;

    private SecurityLevel(int code) {
        this.levelCode = code;
    }

    /**
     * Return the protocol representation of enum.
     * 
     * @return <code>int</code> corresponding protocol code.
     */
    public int getCode() {
        return levelCode;
    }

}
