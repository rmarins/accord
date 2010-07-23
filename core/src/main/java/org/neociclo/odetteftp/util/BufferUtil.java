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
package org.neociclo.odetteftp.util;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class BufferUtil {

    public static void bufferSkip(ByteBuffer buffer, int count) {
        buffer.position(buffer.position() + count);
    }

    public static String toHexString(byte b) {
        String hex = Integer.toHexString((int) b & 0xff);
        if (hex.length() == 1) {
            return ('0' + hex);
        } else {
            return hex;
        }
    }

    public static String toHexString(byte[] barray) {
        return toHexString(barray, -1);
    }
    
    public static String toHexString(byte[] barray, int max) {
        if (max <= 0) {
            max = barray.length;
        }
        StringBuffer hexArray = new StringBuffer("Hex[");
        for (int i=1; i<=max; i++) {
            byte b = barray[i-1];
            hexArray.append(toHexString(b));
            if (i < max) {
                if (i % 5 == 0)
                    hexArray.append("   ");
                else {
                    hexArray.append(" ");
                }
            }
        }
        hexArray.append("]");
        return hexArray.toString();
    }

    public static int seekWithinBuffer(byte[] pattern, ByteBuffer buffer, int limitCount) {

        int startPos = buffer.position();
        byte[] frame = new byte[pattern.length];

        for (int i = 0; i < limitCount; i++) {
            shiftLeft(frame);
            frame[frame.length - 1] = buffer.get(startPos + i);
            if (Arrays.equals(frame, pattern)) {
                return (i - pattern.length + 1);
            }
        }

        return -1;
    }

    private static void shiftLeft(byte[] array) {
        if (array.length <= 1)
            return;
        System.arraycopy(array, 1, array, 0, array.length - 1);
    }

    /**
     * @param buffer
     * @param limitCount
     * @param minRepeat
     * @return The start position of sequence repetition in the buffer. Returns
     *         <code>-1</code> if no repeat sequence is found.
     */
    public static int seekRepeatSequence(ByteBuffer buffer, int limitCount, int minRepeat) {

        if (limitCount < minRepeat)
            return -1;

        int startPos = buffer.position();

        for (int i=0; i<(limitCount - minRepeat); i++) {

            byte octet = buffer.get(startPos + i);
            boolean matched = true;
            for (int k=0; k<minRepeat; k++) {
                if (octet == buffer.get(startPos + i + k + 1)) {
                    matched &= true;
                } else {
                    matched = false;
                    break;
                }
            }

            if (matched) {
                return (startPos + i);
            }

        }

//        int repeatPos = -1;
//        int limitControl = 0;
//        int sequenceCounter = 0;
//        byte lastOctet = 0;
//
//
//        int currentPos = 0;
//
//        do {
//            currentPos = startPos + limitControl;
//            byte octet = buffer.get(currentPos);
//
//            if (lastOctet == octet) {
//                repeatPos = currentPos - 1;
//                sequenceCounter++;
//            }
//
//            /* Repetition sequence found within buffer. */
//            if (sequenceCounter >= (minRepeat - 1)) {
//                return repeatPos;
//            }
//
//            limitControl++;
//            lastOctet = octet;
//
//        } while (buffer.limit() > currentPos && limitControl <= limitCount);

        return -1;
    }

    public static int getRepeatSequenceCount(ByteBuffer buffer, int limitCount) {

        int counter = 0;

        int startPos = buffer.position();
        byte lastOctet = buffer.get(startPos);

        int currentPos = 0;

        do {
            currentPos = startPos + counter;
            byte octet = buffer.get(currentPos);

            if (lastOctet != octet)
                break;

            counter++;
        } while (buffer.limit() > currentPos && counter <= limitCount);

        return counter;

    }

}
