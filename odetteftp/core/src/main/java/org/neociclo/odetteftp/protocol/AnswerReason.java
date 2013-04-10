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
 * Enumeration that represents each reason code for available Answer Reasons
 * while transferring a Virtual File.
 * <p/>
 * Answer Codes
 *
 * <pre>
 *        Value: 01 - Invalid filename.
 *               02 - Invalid destination.
 *               03 - Invalid origin.
 *               04 - Storage record format not supported.
 *               05 - Maximum record length not supported.
 *               06 - File size is too big.
 *               10 - Invalid record count.
 *               11 - Invalid byte count.
 *               12 - Access method failure.
 *               13 - Duplicate file.
 *               14 - File direction refused (since ODETTE FTP version 1.4).
 *               99 - Unspecified reason.
 * </pre>
 *
 * Following Answer Reasons are supported since ODETTE FTP v2.0:
 * <p/>
 *
 * ODETTE FTP v2.0 Extended Answer Codes
 *
 * <pre>
 *        Value: 15 - Cipher suite not supported.
 *               16 - Encrypted file not allowed.
 *               17 - Unencrypted file not allowed.
 *               18 - Compression not allowed.
 *               19 - Signed file not allowed.
 *               20 - Unsigned file not allowed.
 *               21 - Invalid file signature
 *               22 - File decryption failure
 *               23 - File decompression failure
 * </pre>
 *
 * @author Rafael Marins
 */
public enum AnswerReason {

    /**
     * Access method failure.
     */
    ACCESS_METHOD_FAILURE(12),
    
    CIPHER_SUITE_NOT_SUPPORTED(15),
    
    COMPRESSION_NOT_ALLOWED(18),

    /**
     * Duplicate file.
     */
    DUPLICATE_FILE(13),

    ENCRYPTED_FILE_NOT_ALLOWED(16),
    
    FILE_DECOMPRESSION_FAILURE(23),
    
    FILE_DECRYPTION_FAILURE(22),
    
    /**
     * File direction refused. Valid only on ODDETE-FTP version 1.4 and above.
     */
    FILE_DIRECTION_REFUSED(14),

    /**
     * File size is too big.
     */
    FILE_SIZE_EXCEED(6),

    /**
     * Invalid byte count.
     */
    INVALID_BYTE_COUNT(11),

    /**
     * Invalid destination.
     */
    INVALID_DESTINATION(2),
    
    INVALID_FILE_SIGNATURE(21),

    /**
     * Invalid filename.
     */
    INVALID_FILENAME(1),

    /**
     * Invalid origin.
     */
    INVALID_ORIGIN(3),

    /**
     * Invalid record count.
     */
    INVALID_RECORD_COUNT(10),
    
    SIGNED_FILE_NOT_ALLOWED(19),
    
    UNENCRYPTED_FILE_NOT_ALLOWED(17),
    
    UNSIGNED_FILE_NOT_ALLOWED(20),

    /**
     * Unspecified reason.
     */
    UNSPECIFIED(99),

    /**
     * Maximum record length not supported.
     */
    UNSUPPORTED_MAXIMUM_RECORD_LENGTH(5),

    /**
     * Storage record format not supported.
     */
    UNSUPPORTED_STORAGE_RECORD_FORMAT(4);

    /**
     * Convenient method for parsing the proper AnswerReason instance given a
     * code String.
     * 
     * @param code
     *        The answer reason string being evaluated
     * @return AnswerReason Instance that correspond to the parameter
     * @throws OdetteFtpException
     * @throws CommandNotRecognisedException
     *         Command not recognized
     */
    public static AnswerReason parse(int code) throws OdetteFtpException {
        AnswerReason found = null;

        for (AnswerReason ar : AnswerReason.values()) {
            if (ar.getCode() == code) {
                found = ar;
                break;
            }
        }

        if (found == null) {
            throw new CommandNotRecognisedException("Answer Reason not recognised: " + code);
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
    private AnswerReason(int aReasonCode) {
        reasonCode = aReasonCode;
    }

    /**
     * Return the protocol representation of AnswerReason enum.
     * 
     * @return <code>String</code> corresponding protocol code.
     */
    public int getCode() {
        return reasonCode;
    }

}
