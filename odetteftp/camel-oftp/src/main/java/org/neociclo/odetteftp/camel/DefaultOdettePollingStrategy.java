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
