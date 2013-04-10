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
package org.neociclo.odetteftp.util;

import static org.neociclo.odetteftp.EntityState.LISTENER;
import static org.neociclo.odetteftp.EntityState.SPEAKER;
import static org.neociclo.odetteftp.EntityType.INITIATOR;
import static org.neociclo.odetteftp.EntityType.RESPONDER;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_RECORD_SIZE;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.neociclo.odetteftp.EntityState;
import org.neociclo.odetteftp.EntityType;
import org.neociclo.odetteftp.protocol.RecordFormat;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class ProtocolUtil {

    /**
     * Create date from given values. Local timezone is used in returning date.
     * 
     * @param year
     *            Four* digit year. When single decimals is used, set the
     *            current century from local date time.
     * @param month
     *            From January (1) to December (12)
     * @param day
     *            Day of the month.
     * @param hour
     *            24 hour format.
     * @param minute
     * @param second
     * @param millisecond
     * @return
     */
    public static Date createDate(int year, int month, int day, int hour, int minute, int second, int millisecond) {

        Calendar cal = Calendar.getInstance();

        // Must guarantee that the century is set.
        if ((year / 100) == 0) {
            // Use century from local date time Calendar
            int century = cal.get(Calendar.YEAR) / 100;
            year = (century * 100) + year;
        }

        // Convert conventional month range to Calendar month range.
        month--;

        cal.set(year, month, day, hour, minute, second);
        cal.set(Calendar.MILLISECOND, millisecond);

        return cal.getTime();
    }

    public static String formatDate(String pattern, Date value) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(value);
    }

    public static String padd(String value, int length, boolean left, char completingChar) {

        if (value == null)
            value = "";

        StringBuffer paddText = new StringBuffer(value);

        if (value.length() < length) {

            int count = length - value.length();
            for (int i = 0; i < count; i++) {
                if (left) {
                    paddText.insert(0, completingChar);
                } else {
                    paddText.append(completingChar);
                }
            }
        }

        return paddText.toString();
    }

    public static byte[] formatBinaryNumber(int number, int size) {
        byte[] bin = new byte[size];
        int work = number;
        for (int i = (size - 1); i >= 0; i--) {
            int b = (work & 0xff);
            bin[i] = (byte) b;
            work = (work >> 8);
        }
        return bin;
    }

    public static int parseBinaryNumber(byte[] bin) {
        int num = 0;
        for (int i = 0; i < bin.length; i++) {
            if (i > 0)
                num = num << 8;
            num |= bin[i] & 0xff;
        }
        return num;
    }

    public static final boolean valueOfYesNo(String parameter) {
        return ("Y".equals(parameter) ? true : false);
    }

    public static EntityState getInitialState(EntityType entity) {
        if (entity == INITIATOR) {
            return SPEAKER;
        } else if (entity == RESPONDER) {
            return LISTENER;
        } else {
            return null;
        }
    }

    public static long computeVirtualFileOffset(long position, RecordFormat recordFormat, int recordSize) {

        /*
         * Calculate Virtual File record count to submit end file. If record
         * format is UNSTRUCTURED or TEXTFILE force it to 0 (zero).
         */
        long offset = 0;
        int blockSize = OdetteFtpConstants.DEFAULT_RECORD_SIZE;
        if ((recordFormat == RecordFormat.FIXED) || (recordFormat == RecordFormat.VARIABLE)) {
            blockSize = recordSize;
        }

        long octets = position;

        // avoid dividing zero or division by zero
        if (octets == 0 || blockSize == 0)
            return 0;

        offset = (octets / blockSize);

        return offset;

    }

    public static long computeVirtualFileSize(long unitCount) {

        // avoid dividing zero or division by zero
        if (unitCount == 0)
            return 0;

        long fileSize = (unitCount / DEFAULT_RECORD_SIZE);
        if ((unitCount % DEFAULT_RECORD_SIZE) > 0) {
            fileSize++;
        }

        return fileSize;
    }

    public static long computeVirtualFileRecordCount(long unitCount, RecordFormat recordFormat, int recordSize) {

        /*
         * Calculate Virtual File record count to submit end file. If record
         * format is UNSTRUCTURED or TEXTFILE force it to 0 (zero).
         */
        long recordCount = 0;

        if ((recordFormat == RecordFormat.FIXED) || (recordFormat == RecordFormat.VARIABLE)) {

            long octets = unitCount;

            // avoid dividing zero or division by zero
            if (octets == 0 || recordSize == 0)
                return 0;

            recordCount = (octets / recordSize);
            if ((octets % recordSize) > 0) {
                recordCount++;
            }

        }

        return recordCount;
    }

    public static long computeOffsetFilePosition(long answerCount, RecordFormat recordFormat, int recordSize) {

        int blockSize = DEFAULT_RECORD_SIZE;
        if (recordSize > 0 && (recordFormat == RecordFormat.FIXED || recordFormat == RecordFormat.VARIABLE)) {
            blockSize = recordSize;
        }

        return (answerCount * blockSize);
    }

    public static long computeFileSizeInOctets(long size, RecordFormat recordFormat, int recordSize) {

        int blockSize = DEFAULT_RECORD_SIZE;
        if (recordSize > 0 && (recordFormat == RecordFormat.FIXED || recordFormat == RecordFormat.VARIABLE)) {
            blockSize = recordSize;
        }

        return (size * blockSize);
    }

}
