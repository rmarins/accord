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
package org.neociclo.odetteftp.support;

import java.io.IOException;

import org.neociclo.odetteftp.protocol.EndSessionReason;
import org.neociclo.odetteftp.security.OneToOneHandler;
import org.neociclo.odetteftp.security.PasswordAuthenticationCallback;

/**
 * @author Rafael Marins
 */
public abstract class PasswordAuthenticationHandler implements OneToOneHandler<PasswordAuthenticationCallback> {

	public PasswordAuthenticationHandler() {
		super();
	}

	public void handle(PasswordAuthenticationCallback cb) throws IOException {

		if (authenticate(cb.getUser(), cb.getPassword())) {
			cb.setSuccess();
		} else {
			cb.setFailed(getCause());
		}

	}

	public abstract boolean authenticate(String user, String password) throws IOException;

	public abstract EndSessionReason getCause();

}
