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
package org.neociclo.odetteftp;

/**
 * <code>OdetteFTPException</code> is the top-level exception thrown by all
 * Odette-J classes.
 * 
 * @author Rafael Marins
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
