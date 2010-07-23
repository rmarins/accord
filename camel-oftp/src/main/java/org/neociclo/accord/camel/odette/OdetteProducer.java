package org.neociclo.accord.camel.odette;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ObjectHelper;

public class OdetteProducer extends DefaultProducer {

	private OdetteOperations operations;

	protected OdetteProducer(OdetteEndpoint endpoint,
			OdetteOperations operations) {
		super(endpoint);

		this.operations = operations;
	}

	public void process(Exchange exchange) throws Exception {
		List<Exchange> grouped = exchange.getProperty(
				Exchange.GROUPED_EXCHANGE, List.class);

		if (grouped != null) {
			for (Exchange e : grouped) {
				prepareExchange(e);
			}
		} else {
			prepareExchange(exchange);
		}

		operations.startSession();
	}

	private void prepareExchange(Exchange e) {
		Message message = e.getIn();
		ObjectHelper.isAssignableFrom(OdetteMessage.class, message.getClass());

		if (message instanceof OdetteDeliveryMessage) {
			OdetteDeliveryMessage odm = (OdetteDeliveryMessage) message;
			operations.sendDeliveryNotification(odm.getOdetteObject());
		} else if (message instanceof OdetteFileMessage) {
			OdetteFileMessage odm = (OdetteFileMessage) message;
			operations.sendOftpFile(odm.getOdetteObject());
		}
	}
}