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
package org.neociclo.odetteftp;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public enum TransferMode {

    BOTH('B'),

    RECEIVER_ONLY('R'),

    SENDER_ONLY('S');

    public static TransferMode parse(char mode) {
        TransferMode found = null;
        for (TransferMode tm : TransferMode.values()) {
            if (mode == tm.getCode()) {
                found = tm;
                break;
            }
        }

        if (found == null) {
            throw new IllegalArgumentException("Illegal value for TransferMode: " + mode);
        }

        return found;
    }

    public static TransferMode parse(String modeChar) {
        return parse(modeChar.charAt(0));
    }

    private char mode;

    private TransferMode(char aMode) {
        mode = aMode;
    }

    public char getCode() {
        return mode;
    }

    public TransferMode getReversed() {
        TransferMode reversed = null;

        if (this == SENDER_ONLY) {
            reversed = RECEIVER_ONLY;
        } else if (this == RECEIVER_ONLY) {
            reversed = SENDER_ONLY;
        } else {
            reversed = BOTH;
        }

        return reversed;
    }

    public char getReversedCode() {

        char reversed = BOTH.getCode();

        if (this == SENDER_ONLY)
            reversed = RECEIVER_ONLY.getCode();
        else if (this == RECEIVER_ONLY)
            reversed = SENDER_ONLY.getCode();

        return reversed;
    }

}
