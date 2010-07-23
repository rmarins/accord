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
package org.neociclo.odetteftp.protocol;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public enum RecordFormat {

    /**
     * Each record in the file has the same length.
     */
    FIXED("F"),

    /**
     * A Text File is defined as a sequence of ASCII characters, containing no
     * control characters except CR/LF which delimits lines. A line will not
     * have more than 2048 characters.
     */
    TEXTFILE("T"),

    /**
     * The file contains a stream of data. No structure is defined.
     */
    UNSTRUCTURED("U"),

    /**
     * The records in the file can have different lengths.
     */
    VARIABLE("V");

    /**
     * Convenient method for parsing the proper RecordFormat enum given a code
     * character.
     * 
     * @param code
     *        The record format being evaluated
     * @return RecordFormat Instance that correspond to the parameter
     * @throws IllegalArgumentException
     *         Record Format not recognised
     */
    public static RecordFormat parse(String code) {
        RecordFormat found = null;

        for (RecordFormat rf : RecordFormat.values()) {
            if (rf.code.equals(code)) {
                found = rf;
                break;
            }
        }

        if (found == null) {
            throw new IllegalArgumentException("Unknown record format: " + code);
        }

        return found;
    }

    private String code;

    /**
     * Enumeration private constructor with record format code.
     * 
     * @param aCode
     *        Unique class instance identifier.
     */
    private RecordFormat(String formatCode) {
        code = formatCode;
    }

    public String getCode() {
        return code;
    }

}
