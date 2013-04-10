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
package org.neociclo.odetteftp.oftplet;

import static org.neociclo.odetteftp.OdetteFtpVersion.*;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.security.SecurityContext;


/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public abstract class OftpletAdapter implements Oftplet {

    public void destroy() {
    }

    public OftpletListener getListener() {
        return null;
    }

    public SecurityContext getSecurityContext() {
        return null;
    }

    public OftpletSpeaker getSpeaker() {
        return null;
    }

    public void init(OdetteFtpSession session) throws OdetteFtpException {
    }

    public void onExceptionCaught(Throwable cause) {
    }

    public void onSessionStart() {
    }

    public void onSessionEnd() {
    }

    public boolean isProtocolVersionSupported(OdetteFtpVersion version) {
        return (OFTP_V12 == version || OFTP_V13 == version || OFTP_V14 == version || OFTP_V20 == version);
    }


}
