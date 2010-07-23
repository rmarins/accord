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
 * <code>OdetteFTPException</code> is the top-level exception thrown by all
 * Odette-J classes.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OdetteFtpException extends Exception {

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor with no detail message.
     */
    public OdetteFtpException() {
        super();
    }

    /**
     * Constructor with the specified detail message.
     * 
     * @param message
     *        The detail message.
     */
    public OdetteFtpException(String message) {
        super(message);
    }

    /**
     * Constructor with the specified detail message and throwable cause.
     * 
     * @param message
     *        The detail message.
     * @param cause
     *        Throwable that generated this exception.
     */
    public OdetteFtpException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with the specified cause of the exception.
     * 
     * @param cause
     *        Throwable that generated this exception.
     */
    public OdetteFtpException(Throwable cause) {
        super(cause);
    }
}
