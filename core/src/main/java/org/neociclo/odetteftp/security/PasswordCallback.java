/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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
public class PasswordCallback implements Callback {

    private String prompt;

    private String defaultPassword;

    private String username;

    private String password;

    public PasswordCallback() {
        this(null);
    }

    public PasswordCallback(String prompt) {
        this(prompt, null);
    }

    public PasswordCallback(String prompt, String defaultPassword) {
        super();
        this.prompt = prompt;
        this.defaultPassword = defaultPassword;
    }

    public String getUsername() {
        return (username == null ? prompt : username);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return (password == null ? defaultPassword : password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPrompt() {
        return prompt;
    }

}
