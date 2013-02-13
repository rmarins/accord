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
 * @author Rafael Marins
 * @version $Rev$ $Date$
 * @since OdetteJ API 1.0
 */
public class AuthenticationException extends OdetteFtpException {

    public static final int INVALID_PASSWORD = 2;

    public static final int UNKNOWN_USER_CODE = 1;
    public static final int UNSPECIFIED = 0;
    private static final long serialVersionUID = 1L;

    private int failure;

    public AuthenticationException(int failureCode) {
        super();
        failure = failureCode;
    }

    public AuthenticationException(int failureCode, String message) {
        super(message);
        failure = failureCode;
    }

    public AuthenticationException(int failureCode, String message, Throwable cause) {
        super(message, cause);
        failure = failureCode;
    }

    public AuthenticationException(int failureCode, Throwable cause) {
        super(cause);
        failure = failureCode;
    }

    public int getFailure() {
        return failure;
    }
}
