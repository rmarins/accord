package org.neociclo.accord.camel.odette;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultMessage;

abstract class OdetteMessage<T> extends DefaultMessage {

	protected void bind(Exchange exchange) {
		// TODO set headers
	}

	public abstract T getOdetteObject();

}
