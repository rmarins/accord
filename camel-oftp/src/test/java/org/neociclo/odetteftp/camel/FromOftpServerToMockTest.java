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
 * @version $Rev$ $Date$
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
