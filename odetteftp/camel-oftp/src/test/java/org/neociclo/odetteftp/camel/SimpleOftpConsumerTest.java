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
