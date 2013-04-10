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

import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V12;
import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V13;
import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V14;
import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V20;

import org.neociclo.odetteftp.protocol.v13.OdetteFtpVer13Handler;
import org.neociclo.odetteftp.protocol.v14.OdetteFtpVer14Handler;
import org.neociclo.odetteftp.protocol.v20.OdetteFtpVer20Handler;

/**
 * @author Rafael Marins
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
