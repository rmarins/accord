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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Map backed implementation of the CallbackHandler - handlers are matched by the callback types.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class MappedCallbackHandler implements CallbackHandler {

    private Map<Class<? extends Callback>, OneToOneHandler<Callback>> handlers;

    public MappedCallbackHandler() {
        super();
        this.handlers = new ConcurrentHashMap<Class<? extends Callback>, OneToOneHandler<Callback>>();
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback cb : callbacks) {
            Class<?> clazz = cb.getClass();
            if (handlers.containsKey(clazz)) {
                OneToOneHandler<Callback> h = handlers.get(clazz);
                h.handle(cb);
            } else {
                throw new UnsupportedCallbackException(cb);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Callback> void addHandler(Class<T> type, OneToOneHandler<T> handler) {
        handlers.put(type, (OneToOneHandler<Callback>) handler);
    }

    public <T extends Callback> void removeHandler(Class<Callback> type) {
        handlers.remove(type);
    }

}
