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
