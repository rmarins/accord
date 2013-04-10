/**
 * Neociclo Accord, Open Source B2Bi Middleware
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

    private Map<Class<? extends Callback>, OneToOneHandler<? extends Callback>> handlers;

    public MappedCallbackHandler() {
        super();
        this.handlers = new ConcurrentHashMap<Class<? extends Callback>, OneToOneHandler<? extends Callback>>();
    }

    @SuppressWarnings("unchecked")
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback cb : callbacks) {
            Class<?> clazz = cb.getClass();
            if (handlers.containsKey(clazz)) {
                OneToOneHandler<Callback> h = (OneToOneHandler<Callback>) handlers.get(clazz);
                h.handle(cb);
            } else {
                throw new UnsupportedCallbackException(cb);
            }
        }
    }

    public <T extends Callback> void addHandler(Class<T> type, OneToOneHandler<T> handler) {
        handlers.put(type, handler);
    }

    public <T extends Callback> void removeHandler(Class<Callback> type) {
        handlers.remove(type);
    }

	public boolean containsHandler(Class<? extends Callback> type) {
		return handlers.containsKey(type);
	}

}
