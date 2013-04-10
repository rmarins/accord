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

/**
 * @author Rafael Marins
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
