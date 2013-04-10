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
