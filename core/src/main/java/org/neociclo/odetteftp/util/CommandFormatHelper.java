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
package org.neociclo.odetteftp.util;

import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V12;
import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V13;
import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V14;
import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V20;

import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.protocol.CommandFormat;
import org.neociclo.odetteftp.protocol.CommandIdentifier;
import org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13;
import org.neociclo.odetteftp.protocol.v14.ReleaseFormatVer14;
import org.neociclo.odetteftp.protocol.v20.ReleaseFormatVer20;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class CommandFormatHelper {

    public static CommandFormat resolveByVersion(OdetteFtpVersion version, CommandIdentifier identifier) {

        if (identifier == CommandIdentifier.DATA)
            return null;

        CommandFormat format = null;

        if ((version == OFTP_V12) || (version == OFTP_V13))
            format = ReleaseFormatVer13.getFormat(identifier);

        else if (version == OFTP_V14)
            format = ReleaseFormatVer14.getFormat(identifier);

        else if (version == OFTP_V20)
            format = ReleaseFormatVer20.getFormat(identifier);

        return format;
    }

}
