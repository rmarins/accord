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
 */
package org.neociclo.odetteftp.camel;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.camel.util.jndi.CamelInitialContextFactory;

public class InitialTestContextFactory extends CamelInitialContextFactory {

	private static Hashtable<String, Object> beans = new Hashtable<String, Object>();

	public static void addBean(String name, Object bean) {
		beans.put(name, bean);
	}

	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
		Context initialContext = super.getInitialContext(environment);

		for (Map.Entry<String, Object> e : beans.entrySet()) {
			initialContext.bind(e.getKey(), e.getValue());
		}

		return initialContext;
	}
}
