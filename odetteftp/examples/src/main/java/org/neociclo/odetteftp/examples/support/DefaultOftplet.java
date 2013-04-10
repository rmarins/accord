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
package org.neociclo.odetteftp.examples.support;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.oftplet.OftpletAdapter;
import org.neociclo.odetteftp.protocol.EndSessionException;
import org.neociclo.odetteftp.security.DefaultSecurityContext;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.SecurityContext;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;

/**
 * @author Rafael Marins
 * @version $Rev$
 */
public class DefaultOftplet extends OftpletAdapter {

	private OdetteFtpConfiguration conf;
	private SecurityContext securityContext;

	public DefaultOftplet(OdetteFtpConfiguration conf, MappedCallbackHandler callbackHandler) {
		this.conf = conf;
		this.securityContext = new DefaultSecurityContext(callbackHandler);
	}

	@Override
	public void init(OdetteFtpSession session) throws OdetteFtpException {
		// use the SessionConfig object to configure session parameters
		if (conf != null) {
			conf.setup(session);
		}
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
			cause.printStackTrace();
		}
	}
}
