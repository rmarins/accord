/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2010 Neociclo, http://www.neociclo.com
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
package org.neociclo.odetteftp.examples.support;

import java.util.Queue;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.oftplet.Oftplet;
import org.neociclo.odetteftp.oftplet.OftpletAdapter;
import org.neociclo.odetteftp.oftplet.OftpletListener;
import org.neociclo.odetteftp.oftplet.OftpletSpeaker;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.security.SecurityContext;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class InOutSharedQueueOftplet extends OftpletAdapter implements Oftplet {

    private SessionConfig config;
    private SecurityContext securityContext;
    private SharedQueueOftpletListener listener;
    private SharedQueueOftpletSpeaker speaker;

    public InOutSharedQueueOftplet(SessionConfig sessionConfig, Queue<OdetteFtpObject> outgoing,
            Queue<OdetteFtpObject> outgoingDone, Queue<OdetteFtpObject> incoming) {
        super();
        this.config = sessionConfig;
        this.securityContext = new ConfigBasedSecurityContext(sessionConfig);
    	this.listener = new SharedQueueOftpletListener(incoming);
        if (outgoing != null) {
        	this.speaker = new SharedQueueOftpletSpeaker(outgoing, outgoingDone);
        }
    }

    @Override
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    @Override
    public void init(OdetteFtpSession s) throws OdetteFtpException {
        config.setup(s);
    }

    @Override
    public boolean isProtocolVersionSupported(OdetteFtpVersion version) {
        if (config.getVersion() != null) {
            return config.getVersion().equals(version);
        } else {
            return super.isProtocolVersionSupported(version);
        }
    }

    @Override
    public OftpletListener getListener() {
        return listener;
    }

    @Override
    public OftpletSpeaker getSpeaker() {
        return speaker;
    }

    public void setEventListener(InOutOftpletEventListener eventListener) {
    	if (speaker != null) {
    		speaker.setEventListenet(eventListener);
    	}
    	if (listener != null) {
    		listener.setEventListener(eventListener);
    	}
    }

}
