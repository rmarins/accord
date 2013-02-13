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

import org.neociclo.odetteftp.OdetteFtpException;

/**
 * The exception thrown when parsing or using an Exchange Buffer that contains
 * an invalid command code (1st octet of the buffer).
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
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
