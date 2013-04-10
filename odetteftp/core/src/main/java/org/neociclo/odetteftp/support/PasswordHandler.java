/**
 * Neociclo Accord, Open Source B2B Integration Suite
 * Copyright (C) 2005-2010 Neociclo, http://www.neociclo.com
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
package org.neociclo.odetteftp.support;

import java.io.IOException;

import org.neociclo.odetteftp.security.OneToOneHandler;
import org.neociclo.odetteftp.security.PasswordCallback;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class PasswordHandler implements OneToOneHandler<PasswordCallback> {

	private transient String username;
	private transient String password;

	public PasswordHandler(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public void handle(PasswordCallback cb) throws IOException {

		cb.setUsername(username);
		cb.setPassword(password);

	}

}
