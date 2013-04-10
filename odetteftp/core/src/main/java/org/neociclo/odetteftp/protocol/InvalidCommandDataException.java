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
 * This exception is thrown when a command contains invalid data. A field within
 * the command may be wrong formatted or have unexpected data.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class InvalidCommandDataException extends OdetteFtpException {

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor with no detail message.
     */
    public InvalidCommandDataException() {
        super();
    }

    /**
     * Constructor with the specified detail message.
     * 
     * @param message
     *        The detail message.
     */
    public InvalidCommandDataException(String message) {
        super(message);
    }
}
