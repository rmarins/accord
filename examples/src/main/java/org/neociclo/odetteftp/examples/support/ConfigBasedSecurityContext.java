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
package org.neociclo.odetteftp.examples.support;

import java.io.IOException;

import javax.security.auth.callback.CallbackHandler;

import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.SecurityContext;
import org.neociclo.odetteftp.security.OneToOneHandler;
import org.neociclo.odetteftp.security.PasswordCallback;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class ConfigBasedSecurityContext implements SecurityContext {

    private SessionConfig config;
    private MappedCallbackHandler callbackHandler;

    public ConfigBasedSecurityContext(SessionConfig sessionConfig) {
        super();
        this.config = sessionConfig;
    }

    public CallbackHandler getCallbackHandler() {

        if (callbackHandler == null) {
            callbackHandler = new MappedCallbackHandler();

            // respond callback retrieval with Usercode & Password
            callbackHandler.addHandler(PasswordCallback.class, new OneToOneHandler<PasswordCallback>() {
                public void handle(PasswordCallback cb) throws IOException {
                    cb.setUsername(config.getUserCode());
                    cb.setPassword(config.getUserPassword());
                }
            });

        }
        return callbackHandler;
    }

}
