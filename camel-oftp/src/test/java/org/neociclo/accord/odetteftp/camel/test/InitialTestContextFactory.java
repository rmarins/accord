package org.neociclo.accord.odetteftp.camel.test;

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
