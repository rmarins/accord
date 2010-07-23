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
package org.neociclo.odetteftp.oftplet;

import static org.neociclo.odetteftp.OdetteFtpVersion.*;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.protocol.EndSessionReason;
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

    public void onSessionEnd(EndSessionReason reason, String reasonText, boolean localIssued) {
    }

    public void onSessionStart() {
    }

    public void onSessionEnd() {
    }

    public boolean isProtocolVersionSupported(OdetteFtpVersion version) {
        return (OFTP_V12 == version || OFTP_V13 == version || OFTP_V14 == version || OFTP_V20 == version);
    }


}
