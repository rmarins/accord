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
 * Enumeration indicating the algorithm used to compress the file.
 *
 * <pre>
 *    Value: '0'  No compression
 *           '1'  Compressed with [ZLIB] algorithm
 * </pre>
 *
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public enum FileCompression {

    NO_COMPRESSION(0),

    ZLIB(1);

    /**
     * Convenient method for parsing the proper CompressionAlgorithm enum given a
     * protocol code.
     * 
     * @param code
     *        The compression algorithm being evaluated
     * @return CompressionAlgorithm enum that correspond to the given code.
     * @throws OdetteFtpException
     * @throws CommandNotRecognisedException
     *         Code not recognized
     */
    public static FileCompression parse(int code) throws OdetteFtpException {
        FileCompression found = null;

        for (FileCompression ca : FileCompression.values()) {
            if (ca.getCode() == code) {
                found = ca;
                break;
            }
        }

        if (found == null) {
            throw new CommandNotRecognisedException("Compression Algorithm not recognised: " + code);
        }

        return found;
    }

    private int algorithmCode;

    private FileCompression(int code) {
        this.algorithmCode = code;
    }

    /**
     * Return the protocol representation of enum.
     * 
     * @return <code>int</code> corresponding protocol code.
     */
    public int getCode() {
        return algorithmCode;
    }

}
