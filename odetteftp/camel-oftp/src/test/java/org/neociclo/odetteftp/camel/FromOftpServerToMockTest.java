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

import java.io.File;
import java.util.Date;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.neociclo.odetteftp.camel.OftpMessage;
import org.neociclo.odetteftp.camel.test.BaseClientTestSupport;
import org.neociclo.odetteftp.protocol.RecordFormat;

/**
 * @author Rafael Marins
 */
public class FromOftpServerToMockTest extends BaseClientTestSupport {

	@EndpointInject(uri = "mock:result")
	private MockEndpoint resultEndpoint;

	protected String expectedBody = "Hello World!";

	protected String getOftpServerUri() {
		return "oftp-server://O0123MyServerOid@localhost:" + getPort() + "/?password=secret";
	}

//	@Override
//	protected OftpServerEndpoint getEndpoint() {
//		return (OftpServerEndpoint) context.getEndpoint(getOftpServerUri());
//	}

	@Test
	public void testOftpServerRoute() throws Exception {

		resultEndpoint.expectedBodiesReceived(expectedBody);
		resultEndpoint.expectedHeaderReceived(OftpMessage.OFTP_ORIGINATOR, "MyOid");
		resultEndpoint.expectedHeaderReceived(OftpMessage.OFTP_DESTINATION, "DestOid");
		resultEndpoint.expectedHeaderReceived(OftpMessage.OFTP_USER_DATA, null);
		resultEndpoint.expectedHeaderReceived(OftpMessage.OFTP_VF_RECORD_FORMAT, RecordFormat.UNSTRUCTURED);
		resultEndpoint.expectedHeaderReceived(OftpMessage.OFTP_VF_SIZE, 1);

		File payloadFile = createTempFile(expectedBody);

		resultEndpoint.expectedHeaderReceived(OftpMessage.OFTP_TIMESTAMP, new Date(payloadFile.lastModified()));
		String dsn = payloadFile.getName();
		if (dsn.length() > 26) {
			dsn = dsn.substring(0, 26);
		}
		resultEndpoint.expectedHeaderReceived(OftpMessage.OFTP_DATASET_NAME, dsn);

		sendFile(payloadFile, "MyOid", "guest", "DestOid");

		payloadFile.delete();

		resultEndpoint.assertIsSatisfied();

	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(getOftpServerUri())
					.to("log:org.neociclo.accord.odetteftp.camel.FromOftpServerToMockTest.LOG?level=DEBUG" +
							"&showHeaders=true&multiline=true")
					.to("log:LOG_OFTP_PAYLOAD?level=DEBUG&showHeaders=true&multiline=true&showExchangeId=true")
					.to("mock:result");
			}
		};
	}

}
