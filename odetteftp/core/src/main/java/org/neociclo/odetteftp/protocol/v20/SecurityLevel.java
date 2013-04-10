/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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
