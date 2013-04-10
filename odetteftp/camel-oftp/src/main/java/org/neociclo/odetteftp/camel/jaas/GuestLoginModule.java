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
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 */
public class GuestLoginModule implements LoginModule {

	private static final String GUEST_PASSWORD = "org.neociclo.accord.odetteftp.camel.jaas.guest.password";

	private static final Logger LOGGER = LoggerFactory.getLogger(GuestLoginModule.class);

	private CallbackHandler ch;
	private String defaultPassword;
	private Subject subject;
	private boolean debug;

	private Set<Principal> principals = new HashSet<Principal>();

	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
			Map<String, ?> options) {
		this.subject = subject;
		this.ch = callbackHandler;

		debug = "true".equalsIgnoreCase((String) options.get("debug"));
		if (options.get(GUEST_PASSWORD) != null) {
			defaultPassword = (String) options.get(GUEST_PASSWORD);
		}

	}

	public boolean login() throws LoginException {

		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("Usercode: ", "guest");
		callbacks[1] = new PasswordCallback("Password: ", false);

		try {
			ch.handle(callbacks);
		} catch (IOException e) {
			return false;
		} catch (UnsupportedCallbackException e) {
			return false;
		}

		String oid = ((NameCallback) callbacks[0]).getName();
		char[] pwd = ((PasswordCallback) callbacks[1]).getPassword();

		if (debug) {
			LOGGER.debug("login {}", oid);
		}

		if (!"guest".equalsIgnoreCase(new String(pwd))) {
			return false;
		}

		principals.add(new OidPrincipal(oid));
		return true;
	}

	public boolean commit() throws LoginException {
		subject.getPrincipals().addAll(principals);
		if (debug) {
			LOGGER.debug("commit");
		}
		return true;
	}

	public boolean abort() throws LoginException {
		if (debug) {
			LOGGER.debug("abort");
		}
		return true;
	}

	public boolean logout() throws LoginException {
		subject.getPrincipals().removeAll(principals);
		if (debug) {
			LOGGER.debug("logout");
		}
		return true;
	}

}
