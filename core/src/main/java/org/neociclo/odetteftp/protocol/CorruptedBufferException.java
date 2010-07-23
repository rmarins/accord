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
 * This exception is thrown when the processing buffer is corrupted. It almost
 * happen when checking buffer length and its limit ranges, or if the length of
 * the Exchange Buffer as determined by the Stream Transmission Header is
 * different to the length implied by the Command Code.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class CorruptedBufferException extends OdetteFtpException {

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor with no detail message.
     */
    public CorruptedBufferException() {
        super();
    }

    /**
     * Constructor with the specified detail message.
     * 
     * @param message
     *        The detail message.
     */
    public CorruptedBufferException(String message) {
        super(message);
    }
}
