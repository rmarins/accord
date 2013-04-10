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
