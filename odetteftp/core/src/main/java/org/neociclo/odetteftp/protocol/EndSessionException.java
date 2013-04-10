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
import org.neociclo.odetteftp.protocol.EndSessionReason;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 * @since OdetteJ API 1.0
 */
public class EndSessionException extends OdetteFtpException {

    private static final long serialVersionUID = 1L;

    private EndSessionReason reason;

    /**
     * 
     */
    public EndSessionException(EndSessionReason reason) {
        super();

        this.reason = reason;
    }

    /**
     * @param message
     */
    public EndSessionException(EndSessionReason reason, String message) {
        super(message);

        this.reason = reason;
    }

    public EndSessionReason getReason() {
        return reason;
    }
}
