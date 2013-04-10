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
