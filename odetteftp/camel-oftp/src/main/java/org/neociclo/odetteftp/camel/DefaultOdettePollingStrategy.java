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

import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.spi.PollingConsumerPollStrategy;
import org.neociclo.odetteftp.protocol.EndSessionException;

public class DefaultOdettePollingStrategy implements PollingConsumerPollStrategy {

	public boolean begin(Consumer consumer, Endpoint endpoint) {
		OftpClientEndpoint oftpEndpoint = (OftpClientEndpoint) endpoint;

		// is session working? if true, skip polling this time
		if (oftpEndpoint.getOftpClient().isConnected()) {
			return false;
		}

		return true;
	}

	public void commit(Consumer consumer, Endpoint endpoint) {
	}

	public boolean rollback(Consumer consumer, Endpoint endpoint, int retryCounter, Exception cause) throws Exception {

		// only retry under theses error codes: 08, 09, 99
		if (cause instanceof EndSessionException) {
			EndSessionException ese = (EndSessionException) cause;

			switch (ese.getReason()) {
			case UNSPECIFIED_ABORT:
			case TIME_OUT:
			case RESOURCES_NOT_AVAIABLE:
				if (retryCounter == OftpSettings.DEFAULT_POLLING_RETRY_COUNT)
					return true;
			default:
				return false;
			}
		}

		return false;
	}

}
