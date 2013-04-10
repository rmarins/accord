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
package org.neociclo.odetteftp.security;

import javax.security.auth.callback.Callback;

import org.neociclo.odetteftp.protocol.EndSessionReason;

/**
 * @author Rafael Marins
 */
public class PasswordAuthenticationCallback implements Callback {

    private String user;
    private String password;

    private EndSessionReason cause;
    private boolean success;

    public PasswordAuthenticationCallback(String user, String password) {
        super();
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

	public EndSessionReason getCause() {
		return cause;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess() {
		this.success = true;
	}

	public void setFailed() {
		setFailed(EndSessionReason.UNSPECIFIED_ABORT);
	}

	public void setFailed(EndSessionReason cause) {
		this.success = false;
		this.cause = cause;
	}

	@Override
	public String toString() {
		return String.format("PasswordAuthenticationCallback[%s, %s, %s]", success, user, cause);
	}
}
