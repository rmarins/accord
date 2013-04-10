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
