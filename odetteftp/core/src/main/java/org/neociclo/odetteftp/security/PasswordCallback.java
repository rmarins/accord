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

/**
 * @author Rafael Marins
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
