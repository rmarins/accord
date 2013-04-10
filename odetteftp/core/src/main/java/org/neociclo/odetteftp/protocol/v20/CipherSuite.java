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
 * Enumeration indicating the cipher suite used to signe and/or encrypt the file
 * and also to indicate the cipher suite that should be used when a signed EERP
 * or NERP is requested.
 * 
 * <pre>
 *    Value: '00'  No security services
 *                 Symmetric          Asymmetric    Hashing
 *                 -----------------  ------------  -------
 *           '01'  3DES_EDE_CBC_3KEY  RSA_PKCS1_15  SHA-1
 *           '02'  AES_256_CBC        RSA_PKCS1_15  SHA-1
 * </pre>
 * 
 * TripleDES is using Cipher Block Chaining (CBC) mode for added security and
 * uses the Encryption Decryption Encryption (EDE) process with 3 different
 * 64-bit keys.
 * <p/>
 * RSA padding is as defined in [PKCS#1].
 * <p/>
 * AES is using a 256-bit key in CBC mode.
 * 
 * @author Rafael Marins
 */
public enum CipherSuite {

    NO_CIPHER_SUITE_SELECTION(0),

    TRIPLEDES_RSA_SHA1(1),

    AES_RSA_SHA1(2);


    /**
     * Convenient method for parsing the proper CipherSuite enum given a
     * protocol code.
     * 
     * @param code
     *        The cipher suite selection being evaluated
     * @return CipherSuite enum that correspond to the given code.
     * @throws OdetteFtpException
     * @throws CommandNotRecognisedException
     *         Code not recognized
     */
    public static CipherSuite parse(int code) throws OdetteFtpException {
        CipherSuite found = null;

        for (CipherSuite cs : CipherSuite.values()) {
            if (cs.getCode() == code) {
                found = cs;
                break;
            }
        }

        if (found == null) {
            throw new CommandNotRecognisedException("Cipher Suite not recognised: " + code);
        }

        return found;
    }

    private int selectionCode;

    private CipherSuite(int code) {
        this.selectionCode = code;
    }

    /**
     * Return the protocol representation of enum.
     * 
     * @return <code>int</code> corresponding protocol code.
     */
    public int getCode() {
        return selectionCode;
    }

}
