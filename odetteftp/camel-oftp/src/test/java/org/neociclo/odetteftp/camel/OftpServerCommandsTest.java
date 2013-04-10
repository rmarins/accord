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

import static org.neociclo.odetteftp.camel.OftpCommand.*;
import static org.neociclo.odetteftp.camel.OftpMessage.*;

import java.io.File;
import java.util.List;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.neociclo.odetteftp.camel.OftpCommand;
import org.neociclo.odetteftp.camel.OftpMessage;
import org.neociclo.odetteftp.camel.test.BaseClientTestSupport;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpServerCommandsTest extends BaseClientTestSupport {

	protected String expectedBody = "I'm back... Hello World!";

	@EndpointInject(uri = "mock:request")
	private MockEndpoint requestEndpoint;

	@EndpointInject(uri = "mock:reply")
	private MockEndpoint replyEndpoint;

	@Test
	public void testReplyWithSingleExchange() throws Exception {

		// setup & start Camel with this test specific routes
		context.addRoutes(createRequestReplyWithSingleExchangeRoutes());
		startCamelContext();

		// prepare mock:request with assertion conditions
		requestEndpoint.expectedMessageCount(1);
		requestEndpoint.expectedHeaderReceived(OftpMessage.OFTP_SESSION_REMOTE_USER, "MyOid");

		// prepare mock:reply with assertion conditions
		replyEndpoint.expectedMessageCount(1);
		replyEndpoint.expectedBodiesReceived(expectedBody);
		replyEndpoint.expectedHeaderReceived(OftpMessage.OFTP_SESSION_REMOTE_USER, "MyOid");

		// connect and receive files to tempDir
		List<OdetteFtpObject> filesReceived = clientReceiveAllFiles("MyOid", "guest", new SaveToTempOftpListener());

		requestEndpoint.assertIsSatisfied();
		replyEndpoint.assertIsSatisfied();

		assertEquals(1, filesReceived.size());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testReplyWithGroupedExchanges() throws Exception {

		// setup & start Camel with this test specific routes
		context.addRoutes(createRequestReplyWithGroupedExchangeRoutes(5));
		startCamelContext();

		// prepare mock:reply with assertion conditions
		replyEndpoint.expectedMessageCount(1);

		// connect and receive files to tempDir
		List<OdetteFtpObject> filesReceived = clientReceiveAllFiles("MyOid", "guest", new SaveToTempOftpListener());

		// mock:reply - validate it has replied with a grouped exchange
		replyEndpoint.assertIsSatisfied();

		Exchange exchange = replyEndpoint.getExchanges().get(0);
		List<Exchange> grouped = exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class);

		assertEquals(5, grouped.size());

		assertEquals(expectedBody, grouped.get(0).getIn().getBody(String.class));
		assertEquals(expectedBody, grouped.get(1).getIn().getBody(String.class));
		assertEquals(expectedBody, grouped.get(2).getIn().getBody(String.class));
		assertEquals(expectedBody, grouped.get(3).getIn().getBody(String.class));
		assertEquals(expectedBody, grouped.get(4).getIn().getBody(String.class));

		// filesReceived - validate against the received files list
		assertEquals(5, filesReceived.size());

	}

	@Override
	public boolean isUseRouteBuilder() {
		return false;
	}

	protected String getOftpServerUri() {
		return "oftp-server://O0123MyServerOid@localhost:" + getPort() + "/?password=secret&routeCommands=true";
	}

	private RouteBuilder createRequestReplyWithGroupedExchangeRoutes(final int numOfExchangesGrouped) {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {

				from(getOftpServerUri())
					.filter(body().isInstanceOf(OftpCommand.class))
					.filter(header(OFTP_MESSAGE_COMMAND_NAME).isEqualTo(OFTP_CMD_RETRIEVE_USER_OUTGOING_EXCHANGES))
					.to("log:LOG_OFTP_CMD_REQUEST?level=DEBUG&showHeaders=true&multiline=true&showExchangeId=true")
					.to("mock:request")
					.process(new Processor() {
						public void process(final Exchange exchange) throws Exception {

							ProducerTemplate producer = exchange.getContext().createProducerTemplate();

							// reply with a grouped exchanges of payload
							for (int i=0; i<numOfExchangesGrouped; i++) {
								producer.send("direct:reply", new Processor() {
									public void process(Exchange outExchange) throws Exception {
										File payloadFile = createTempFile(expectedBody);
										outExchange.setPattern(ExchangePattern.InOut);
										outExchange.getIn().setHeaders(exchange.getIn().getHeaders());
										outExchange.getIn().removeHeader(OFTP_MESSAGE_COMMAND_NAME);
										outExchange.getIn().setBody(payloadFile);
										payloadFile.deleteOnExit();
									}
								});
							}

						}
					});

				from("direct:reply")
                    // aggregate all using same expression
                    .aggregate(constant(true))
                    // wait for the specified number of exchanges to aggregate
                    .completionSize(numOfExchangesGrouped)
                    // group the exchanges so we get one single exchange containing all the others
                    .groupExchanges()
//					.recipientList(header("OftpReplyTo")) // same as to() but using Expression to evaluate the URI
					.to(getOftpServerUri())
					.to("log:LOG_OFTP_COMMAND_REPLY?level=DEBUG&showHeaders=true&multiline=true&showExchangeId=true")
					.to("mock:reply");
			}
		};
	}


	private RouteBuilder createRequestReplyWithSingleExchangeRoutes() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {

				from(getOftpServerUri())
					.filter(body().isInstanceOf(OftpCommand.class))
					.filter(header(OFTP_MESSAGE_COMMAND_NAME).isEqualTo(OFTP_CMD_RETRIEVE_USER_OUTGOING_EXCHANGES))
					.to("log:LOG_OFTP_CMD?level=DEBUG&showHeaders=true&multiline=true&showExchangeId=true")
					.to("mock:request")
					.to("direct:request");

				from("direct:request")
					.process(new Processor() {
						public void process(Exchange exchange) throws Exception {

							Message in = exchange.getIn();

							// reply with a single payload exchange
							File payloadFile = createTempFile(expectedBody);
							in.removeHeader("OftpCommandName");
							in.setBody(payloadFile);

							exchange.setExchangeId(null); // force new exchangeId

							payloadFile.deleteOnExit();
						}
					})
					.to("log:LOG_OFTP_COMMAND_REPLY?level=DEBUG&showHeaders=true&multiline=true&showExchangeId=true")
					.to("mock:reply");
			}
		};
	}

	@Override
	protected int getShutdownTimeout() {
		return 999999999;
	}
}
