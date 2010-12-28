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
package org.neociclo.odetteftp.camel.jaas;

import java.io.IOException;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.camel.util.ObjectHelper;
import org.neociclo.odetteftp.protocol.EndSessionReason;
import org.neociclo.odetteftp.support.PasswordAuthenticationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class JaasPasswordAuthenticator extends PasswordAuthenticationHandler implements CallbackHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(JaasPasswordAuthenticator.class);

	private String usercode;
	private String password;
	private EndSessionReason cause;
	private String realm;

	public JaasPasswordAuthenticator(String realm) {
		super();
		this.realm = realm;
	}

	@Override
	public EndSessionReason getCause() {
		return cause;
	}

	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		for (Callback cb : callbacks) {
			if (cb instanceof NameCallback) {
				((NameCallback) cb).setName(usercode);
			} else if (cb instanceof PasswordCallback) {
				((PasswordCallback) cb).setPassword(password.toCharArray());
			} else {
				throw new UnsupportedCallbackException(cb);
			}
		}
	}

	@Override
	public boolean authenticate(String user, String password) throws IOException {

		boolean success;

		ObjectHelper.notEmpty(user, "usercode");
		ObjectHelper.notNull(password, "password");

		this.usercode = user;
		this.password = password;

		LoginContext lc;
		Subject subject = new Subject();

		try {
			lc = new LoginContext(realm, subject, this);
		} catch (Throwable t) {
			throw new IOException("JaasPasswordAuthentication failed to load login context; realm=" + realm, t);
		}

		try {
			lc.login();
			success = true;
		} catch (AccountNotFoundException anfe) {
			LOGGER.trace("Unknown user code.", anfe);
			cause = EndSessionReason.UNKNOWN_USER_CODE;
			success = false;
		} catch (LoginException le) {
			// any other LoginException including FailedLoginException
			LOGGER.trace("Invalid password.", le);
			cause = EndSessionReason.INVALID_PASSWORD;
			success = false;
		}

		this.usercode = null;
		this.password = null;

		return success;
	}

}
