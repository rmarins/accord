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
import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.neociclo.odetteftp.camel.test.AccountInfo;
import org.neociclo.odetteftp.camel.test.BaseServerTestSupport;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class SimpleOftpProducerTest extends BaseServerTestSupport {

	private static final String OFTP_URI_PATTERN = "oftp://%s@%s:%d?password=%s";

	private static final String USER = "O0055TESTUSER";
	private static final String PSWD = "NEOCICLO";
	private static final String DEST = "O0055SOMEPARTNER";

	@EndpointInject(uri = "mock:result")
	private MockEndpoint resultEndpoint;

	@Override
	protected void prepareServerTestData() throws Exception {
		// create user account
		addAccount(new AccountInfo(USER, PSWD));
		addAccount(new AccountInfo(DEST, PSWD));
	}

	@Test
	public void testFileToOftp() throws Exception {

		// copy to sourceDir
		File payloadFile = getResourceFile("PAYLOAD1");
		copyToTempDir(payloadFile);

		// using mock:result to hold on complete the route exchange
		resultEndpoint.expectedMessageCount(1);
		resultEndpoint.assertIsSatisfied();
		resultEndpoint.await(10L, TimeUnit.SECONDS);

		OdetteFtpObject[] userMailbox = listDataInMailbox(DEST);
		assertEquals(1, userMailbox.length);

		assertIsInstanceOf(VirtualFile.class, userMailbox[0]);
		VirtualFile vf = (VirtualFile) userMailbox[0];

		assertEquals(payloadFile.getName(), vf.getDatasetName());

	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				fromF("file://%s?delete=true", getTempDir().getPath())
					.setHeader(OftpMessage.OFTP_SESSION_LOCAL_USER, constant(USER))
					.setHeader(OftpMessage.OFTP_DESTINATION, constant(DEST))
					.to("log:LOG_OFTP?level=DEBUG&showHeaders=true&multiline=true&showExchangeId=true&showBody=false")
					.inOut()
					.toF(OFTP_URI_PATTERN, USER, getBindingAddress(), getPort(), PSWD, DEST)
					.to("mock:result");

			}
		};
	}

}
