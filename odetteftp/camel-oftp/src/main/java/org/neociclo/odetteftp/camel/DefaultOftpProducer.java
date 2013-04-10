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
package org.neociclo.odetteftp.camel;

import java.util.List;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 */
public class DefaultOftpProducer extends DefaultProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOftpProducer.class);

	public DefaultOftpProducer(Endpoint endpoint) {
		super(endpoint);
	}

	@Override
	public IOftpEndpoint getEndpoint() {
		return (IOftpEndpoint) super.getEndpoint();
	}

	@SuppressWarnings("unchecked")
	public void process(Exchange exchange) throws Exception {

		List<Exchange> grouped = exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class);
		// grouped exchange
		if (grouped != null) {
			for (Exchange anExchange : grouped) {
				try {
					handle(anExchange);
				} catch (RuntimeCamelException rce) {
					// exception already set - do nothing
				}
			}
		}
		// single exchange
		else {
			handle(exchange);
		}

	}

	private void handle(Exchange exchange) {

		LOGGER.trace("{} - notifying the Odette FTP producer's observers of: {}", this, exchange);

		getEndpoint().getOutgoingRequestsObservable().notifyObservers(exchange);

		if (exchange.getException() != null) {
			throw new RuntimeCamelException(this + " could not handle the Exchange: " + exchange,
					exchange.getException());
		}
	}

}
