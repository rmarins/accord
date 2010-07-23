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

import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V12;
import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V13;
import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V14;
import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V20;

import org.neociclo.odetteftp.protocol.v13.OdetteFtpVer13Handler;
import org.neociclo.odetteftp.protocol.v14.OdetteFtpVer14Handler;
import org.neociclo.odetteftp.protocol.v20.OdetteFtpVer20Handler;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public final class ProtocolHandlerFactory {

    private static ProtocolHandler version13Singleton;

    private static ProtocolHandler version14Singleton;

    private static ProtocolHandler version20Singleton;

    public static ProtocolHandler getProtocolHandlerByVersion(OdetteFtpVersion version) {

        if ((version == OFTP_V12) || (version == OFTP_V13))
            return getVersion13Singleton();
        else if (version == OFTP_V14)
            return getVersion14Singleton();
        else if (version == OFTP_V20)
            return getVersion20Singleton();
        else
            throw new IllegalArgumentException("Unsupported ODETTE FTP version: " + version);
    }

    public static boolean isProtocolVersionSupported(OdetteFtpVersion ver) {
        return ((ver == OFTP_V12) || (ver == OFTP_V13) || (ver == OFTP_V14) || (ver == OFTP_V20));
    }

    private static ProtocolHandler getVersion13Singleton() {
        if (version13Singleton == null)
            version13Singleton = new OdetteFtpVer13Handler();
        return version13Singleton;
    }

    private static ProtocolHandler getVersion14Singleton() {
        if (version14Singleton == null)
            version14Singleton = new OdetteFtpVer14Handler();
        return version14Singleton;
    }

    private static ProtocolHandler getVersion20Singleton() {
        if (version20Singleton == null)
            version20Singleton = new OdetteFtpVer20Handler();
        return version20Singleton;
    }

    private ProtocolHandlerFactory() {
    }
}
