package org.neociclo.accord.camel.odette;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ObjectHelper;

public class OdetteProducer extends DefaultProducer {

	private OdetteOperations operations;

	protected OdetteProducer(OdetteEndpoint endpoint, OdetteOperations operations) {
		super(endpoint);

		this.operations = operations;
	}

	@SuppressWarnings("unchecked")
	public void process(Exchange exchange) throws Exception {
		List<Exchange> grouped = exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class);

		if (grouped != null) {
			for (Exchange e : grouped) {
				prepareExchange(e);
			}
		} else {
			prepareExchange(exchange);
		}

		operations.pollServer();
	}

	private void prepareExchange(Exchange e) {
		Message message = e.getIn();
		ObjectHelper.isAssignableFrom(OdetteMessage.class, message.getClass());
	}
}