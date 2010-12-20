/**
 * Neociclo Accord, Open Source B2B Integration Suite
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
 * @version $Rev$ $Date$
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
