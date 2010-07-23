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
package org.neociclo.odetteftp.security;

import javax.security.auth.callback.Callback;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class PasswordAuthenticationCallback implements Callback {

    public static enum AuthenticationResult {
        SUCCESS,
        UNKNOWN_USER,
        INVALID_PASSWORD;
    }

    private String user;
    private String password;

    private AuthenticationResult result;

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

    public AuthenticationResult getResult() {
        return result;
    }

    public void setResult(AuthenticationResult result) {
        this.result = result;
    }
}
