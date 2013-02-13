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
 */
public class VirtualFileMappingException extends OdetteFtpException {

    private static final long serialVersionUID = 7288050041078513840L;

    public VirtualFileMappingException(String message) {
        super(message);
    }

    public VirtualFileMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public VirtualFileMappingException(Throwable cause) {
        super(cause);
    }

}
