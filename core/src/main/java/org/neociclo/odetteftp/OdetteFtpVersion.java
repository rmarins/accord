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
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public enum OdetteFtpVersion {

    /** ODETTE-FTP version 1.2 as specified by ODETTE Organization */
    OFTP_V12("1.2", 1),

    /** ODETTE FTP version 1.3 as specified in RFC 2204 */
    OFTP_V13("1.3", 2),

    /** ODETTE FTP version 1.4 as detailed in RFC 5024 */
    OFTP_V14("1.4", 4),

    /** ODETTE FTP version 2.0 as specified in RFC 5024 */
    OFTP_V20("2.0", 5);

    public static OdetteFtpVersion parse(int protocolLevel) {
        OdetteFtpVersion found = null;

        for (OdetteFtpVersion ver : OdetteFtpVersion.values()) {
            if (ver.getProtocolLevel() == protocolLevel) {
                found = ver;
            }
        }

        if (found == null) {
            throw new IllegalArgumentException("Unknown ODETTE-FTP protocol level: " + protocolLevel);
        }

        return found;
    }

    private int protocolLevel;

    private String version;

    private OdetteFtpVersion(String ver, int level) {
        version = ver;
        protocolLevel = level;
    }

    public int getProtocolLevel() {
        return protocolLevel;
    }

    @Override
    public String toString() {
        return version;
    }

    public boolean isEqualOrEarlier(OdetteFtpVersion otherVersion) {
        return (protocolLevel <= otherVersion.protocolLevel);
    }

    public boolean isEarlier(OdetteFtpVersion otherVersion) {
        return (protocolLevel < otherVersion.getProtocolLevel());
    }

    public boolean isOlder(OdetteFtpVersion otherVersion) {
        return (protocolLevel > otherVersion.getProtocolLevel());
    }

    public boolean isEqualOrOlder(OdetteFtpVersion otherVersion) {
        return (protocolLevel >= otherVersion.getProtocolLevel());
    }

}
