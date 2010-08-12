/**
 * Neociclo Accord, Open Source B2B Integration Suite
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

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.oftplet.OftpletAdapter;
import org.neociclo.odetteftp.protocol.EndSessionException;
import org.neociclo.odetteftp.security.SecurityContext;

/**
 * @author Rafael Marins
 * @version $Rev$
 */
public class DefaultOftplet extends OftpletAdapter {

	private SessionConfig conf;
	private ConfigBasedSecurityContext securityContext;

	public DefaultOftplet(SessionConfig conf) {
		this.conf = conf;
		this.securityContext = new ConfigBasedSecurityContext(conf);
	}

	@Override
	public void init(OdetteFtpSession session) throws OdetteFtpException {
		// use the SessionConfig object to configure session parameters
		conf.setup(session);
	}

	@Override
	public SecurityContext getSecurityContext() {
		return securityContext;
	}

	@Override
	public void onExceptionCaught(Throwable cause) {
		if (cause instanceof EndSessionException) {
			EndSessionException es = (EndSessionException) cause;
			System.err.println("SESSION ERROR: " + es.getReason());
		} else {
			System.err.print("GENERAL ERROR - " + cause);
		}
	}
}
