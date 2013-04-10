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
 * The exception thrown when parsing or using an Exchange Buffer that contains
 * an invalid command code (1st octet of the buffer).
 * 
 * @author Rafael Marins
 */
public class CommandNotRecognisedException extends OdetteFtpException {

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 1L;

    private char code;

    /**
     * Constructor with the specified detail message.
     * 
     * @param message
     *        The detail message.
     */
    public CommandNotRecognisedException(String message) {
        super(message);
    }

    public CommandNotRecognisedException(char code, String message) {
        super(message);
        this.code = code;
    }

    public char getCode() {
        return code;
    }

}
