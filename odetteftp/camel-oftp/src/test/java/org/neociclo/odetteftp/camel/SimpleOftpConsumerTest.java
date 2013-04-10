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

import java.io.File;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.neociclo.odetteftp.camel.test.AccountInfo;
import org.neociclo.odetteftp.camel.test.BaseServerTestSupport;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class SimpleOftpConsumerTest extends BaseServerTestSupport {

	private static final String OFTP_URI_PATTERN = "oftp://%s@%s:%d?password=%s";

	private static final String USER = "O0055TESTUSER";
	private static final String PSWD = "NEOCICLO";

	@EndpointInject(uri = "mock:result")
	private MockEndpoint resultEndpoint;

	@Override
	protected void prepareServerTestData() throws Exception {
		// create user account
		addAccount(new AccountInfo(USER, PSWD));

		// put a payload in user's mailbox
		File payloadFile = getResourceFile("PAYLOAD1");
		payloadFile = copyToTempDir(payloadFile);

		DefaultVirtualFile vf = new DefaultVirtualFile(payloadFile);
		vf.setOriginator("O0055SOMEPARTNER");
		vf.setDestination(USER);

		storeDataInMailbox(USER, vf);
	}

	@Test
	public void testPolling() throws Exception {

		// configure result expects
		resultEndpoint.expectedMinimumMessageCount(1);
		resultEndpoint.assertIsSatisfied();

	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				fromF(OFTP_URI_PATTERN, USER, getBindingAddress(), getPort(), PSWD)
					.filter(body().isInstanceOf(VirtualFile.class))
					.to("log:LOG_OFTP_PAYLOAD?level=DEBUG&showHeaders=true&multiline=true&showExchangeId=true")
					.to("mock:result");
			}
		};
	}

}
