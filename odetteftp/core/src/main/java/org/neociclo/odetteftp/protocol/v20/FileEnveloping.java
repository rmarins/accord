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
 * Enumeration indicating the enveloping format used in the file.
 * 
 * <pre>
 *    Value: '0'  No compression
 *           '1'  Compressed with [ZLIB] algorithm
 * </pre>
 * 
 * <p/>
 * If the file is encrypted/signed/compressed or is an file for the exchange and
 * revocation of certificates, this field must be set accordingly.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public enum FileEnveloping {

    NO_ENVELOPE(0),

    CMS(1);

    /**
     * Convenient method for parsing the proper FileEnveloping enum given a
     * protocol code.
     * 
     * @param code
     *        The file enveloping format being evaluated
     * @return FileEnveloping enum that correspond to the given code.
     * @throws OdetteFtpException
     * @throws CommandNotRecognisedException
     *         Code not recognized
     */
    public static FileEnveloping parse(int code) throws OdetteFtpException {
        FileEnveloping found = null;

        for (FileEnveloping fe : FileEnveloping.values()) {
            if (fe.getCode() == code) {
                found = fe;
                break;
            }
        }

        if (found == null) {
            throw new CommandNotRecognisedException("File Enveloping Format not recognised: " + code);
        }

        return found;
    }

    private int formatCode;

    private FileEnveloping(int code) {
        this.formatCode = code;
    }

    /**
     * Return the protocol representation of enum.
     * 
     * @return <code>int</code> corresponding protocol code.
     */
    public int getCode() {
        return formatCode;
    }

}
