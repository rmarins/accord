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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpClientConsumer extends ScheduledPollConsumer implements IOftpConsumer {

	private OftpOperations operations;

	public OftpClientConsumer(OftpClientEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.operations = endpoint.getOperations();

//		setPollStrategy(new DefaultOdettePollingStrategy());
		setUseFixedDelay(true);
	}

	@Override
	protected void poll() throws Exception {

		OftpClientEndpoint endpoint = (OftpClientEndpoint) getEndpoint();
		endpoint.setHasIn();

		try {
			operations.runClient();
		} catch (Exception e) {
			getExceptionHandler().handleException(e);
		}

	}

	public void process(Exchange exchange) {
		try {
			getProcessor().process(exchange);
		} catch (Exception e) {
			getExceptionHandler().handleException(e);
		}
	}

}
