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
 * The Negative Response Reason will specify why transmission cannot proceed or
 * why processing of the file failed. It's supported since the ODETTE-FTP
 * version 1.4.
 * <p/>
 * In version 2.0 reason codes between 25 to 34 (inclusive) were
 * added.
 *
 * <pre>
 *
 *    Value  '03'  ESID received with reason code '03'
 *                  (user code not known)
 *           '04'  ESID received with reason code '04'
 *                  (invalid password)
 *           '09'  ESID received with reason code '99'
 *                  (unspecified reason)
 *           '11'  SFNA(RETRY=N) received with reason code '01'
 *                  (invalid file name)
 *           '12'  SFNA(RETRY=N) received with reason code '02'
 *                  (invalid destination)
 *           '13'  SFNA(RETRY=N) received with reason code '03'
 *                  (invalid origin)
 *           '14'  SFNA(RETRY=N) received with reason code '04'
 *                  (invalid storage record format)
 *           '15'  SFNA(RETRY=N) received with reason code '05'
 *                  (maximum record length not supported)
 *           '16'  SFNA(RETRY=N) received with reason code '06'
 *                  (file size too big)
 *           '20'  SFNA(RETRY=N) received with reason code '10'
 *                  (invalid record count)
 *           '21'  SFNA(RETRY=N) received with reason code '11'
 *                  (invalid byte count)
 *           '22'  SFNA(RETRY=N) received with reason code '12'
 *                  (access method failure)
 *           '23'  SFNA(RETRY=N) received with reason code '13'
 *                  (duplicate file)
 *           '24'  SFNA(RETRY=N) received with reason code '14'
 *                  (file direction refused)
 *           '25'  SFNA(RETRY=N) received with reason code '15'
 *                  (cipher suite not supported)
 *           '26'  SFNA(RETRY=N) received with reason code '16'
 *                  (encrypted file not allowed)
 *           '27'  SFNA(RETRY=N) received with reason code '17'
 *                  (unencrypted file not allowed)
 *           '28'  SFNA(RETRY=N) received with reason code '18'
 *                  (compression not allowed)
 *           '29'  SFNA(RETRY=N) received with reason code '19'
 *                  (signed file not allowed)
 *           '30'  SFNA(RETRY=N) received with reason code '20'
 *                  (unsigned file not allowed)
 *           '31'  File signature not valid.
 *           '32'  File decompression failed.
 *           '33'  File decryption failed.
 *           '34'  File processing failed.
 *           '35'  Not delivered to recipient.
 *           '36'  Not acknowledged by recipient.
 *           '50'  Transmission stopped by the operator.
 *           '90'  File size incompatible with recipient's
 *                  protocol version.
 *           '99'  Unspecified reason.
 * </pre>
 * 
 * @author Rafael Marins
 */
public enum NegativeResponseReason {

    /** ESID received with reason code '04' (invalid password) */
    ESID_INVALID_PASSWORD("04"),

    /** ESID received with reason code '99' (unspecified reason) */
    ESID_UNSPECIFIED_REASON("09"),

    /** ESID received with reason code '03' (user code not known) */
    ESID_USER_NOT_KNOWN("03"),

    /** File size incompatible with recipient's protocol version. */
    INCOMPATIBLE_FILE_SIZE("90"),

    /** Not acknowledged by recipient. */
    NOT_ACKNOWLEDGED("36"),

    /** Not delivered to recipient. */
    NOT_DELIVERED("35"),

    /** SFNA(RETRY=N) received with reason code '12' (access method failure) */
    SFNA_ACCESS_METHOD_FAILURE("22"),

    /** SFNA(RETRY=N) received with reason code '13' (duplicate file) */
    SFNA_DUPLICATE_FILE("23"),

    /** SFNA(RETRY=N) received with reason code '14' (file direction refused) */
    SFNA_FILE_DIRECTION_REFUSED("24"),

    /** SFNA(RETRY=N) received with reason code '06' (file size too big) */
    SFNA_FILE_SIZE_EXCEED("16"),

    /** SFNA(RETRY=N) received with reason code '11' (invalid byte count) */
    SFNA_INVALID_BYTE_COUNT("21"),

    /** SFNA(RETRY=N) received with reason code '02' (invalid destination) */
    SFNA_INVALID_DESTINATION("12"),

    /** SFNA(RETRY=N) received with reason code '01' (invalid file name) */
    SFNA_INVALID_FILE_NAME("11"),

    /** SFNA(RETRY=N) received with reason code '03' (invalid origin) */
    SFNA_INVALID_ORIGIN("13"),

    /** SFNA(RETRY=N) received with reason code '15' (cipher suite not supported) */
    SFNA_CIPHER_NOT_SUPPORTED("25"),

    /** SFNA(RETRY=N) received with reason code '16' (encrypted file not allowed) */
    SFNA_ENCRYPTED_FILE_NOT_ALLOWED("26"),

    /** SFNA(RETRY=N) received with reason code '17' (unencrypted file not allowed) */
    SFNA_UNENCRYPTED_FILE_NOT_ALLOWED("27"),

    /** SFNA(RETRY=N) received with reason code '18' (compression not allowed) */
    SFNA_COMPRESSION_NOT_ALLOWED("28"),

    /** SFNA(RETRY=N) received with reason code '19' (signed file not allowed) */
    SFNA_SIGNED_FILE_NOT_ALLOWED("29"),

    /** SFNA(RETRY=N) received with reason code '20' (unsigned file not allowed) */
    SFNA_UNSIGNED_FILE_NOT_ALLOWED("30"),

    /** File signature not valid. */
    INVALID_FILE_SIGNATURE("31"),

    /** File decompression failed. */
    FILE_DECOMPRESSION_FAILED("32"),

    /** File decryption failed. */
    FILE_DECRYPTION_FAILED("33"),

    /** File processing failed. */
    FILE_PROCESSING_FAILED("34"),

    /** SFNA(RETRY=N) received with reason code '10' (invalid record count) */
    SFNA_INVALID_RECORD_COUNT("20"),

    /**
     * SFNA(RETRY=N) received with reason code '05' (maximum record length not
     * supported)
     */
    SFNA_UNSUPPORTED_MAXIMUM_RECORD_LENGTH("15"),

    /**
     * SFNA(RETRY=N) received with reason code '04' (invalid storage record
     * format)
     */
    SFNA_UNSUPPORTED_STORAGE_RECORD_FORMAT("14"),

    /** Transmission stopped by the operator. */
    TRANSMISSION_STOPPED("50"),

    /** Unspecified reason. */
    UNSPECIFIED_REASON("99");

    /**
     * Convenient method for parsing the proper NegativeResponseReason enum
     * given a identifier character.
     * 
     * @param code
     *        The transfer mode character being evaluated
     * @return NegativeResponseReason enum that correspond to the given code.
     * @throws OdetteFtpException
     * @throws CommandNotRecognisedException
     *         Code not recognized
     */
    public static NegativeResponseReason parse(String code) throws OdetteFtpException {
        NegativeResponseReason found = null;

        for (NegativeResponseReason esr : NegativeResponseReason.values()) {
            if (esr.getCode().equals(code)) {
                found = esr;
                break;
            }
        }

        if (found == null) {
            throw new CommandNotRecognisedException("Negative Response Reason not recognized: " + code);
        }

        return found;
    }

    private String reasonCode;

    /**
     * Enumeration constructor where reason code is specified.
     * 
     * @param aReasonCode
     */
    private NegativeResponseReason(String aReasonCode) {
        reasonCode = aReasonCode;
    }

    /**
     * Return the protocol representation of NegativeResponseReason enum.
     * 
     * @return <code>String</code> corresponding protocol code.
     */
    public String getCode() {
        return reasonCode;
    }
}
