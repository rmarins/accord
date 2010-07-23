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
