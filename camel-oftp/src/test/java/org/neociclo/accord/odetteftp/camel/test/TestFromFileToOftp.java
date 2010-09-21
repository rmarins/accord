package org.neociclo.accord.odetteftp.camel.test;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.neociclo.accord.odetteftp.camel.IncomingFileResponse;
import org.neociclo.accord.odetteftp.camel.OdetteEndpoint;
import org.neociclo.accord.odetteftp.camel.OdetteHandler;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * Neociclo Accord - Open Source B2B Integration Suite Copyright (C) 2005-2008
 * Neociclo, http://www.neociclo.com
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: FromFileToOftpTest.java 482 2010-07-23
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class TestFromFileToOftp extends CamelTestSupport {

	@EndpointInject(uri = "mock:result")
	private MockEndpoint resultEndpoint;

	@Produce(uri = "direct:start")
	protected ProducerTemplate template;

	private String oftpToUrl = "oftp://O0055SOFTMIDIA1:8169S412@200.244.109.85:6001?tmpDir=/home/bruno/odette/work&delay=5000";

	// private String oftpToUrl =
	// "oftp://DINET:NEOCICLO@192.168.69.4:3305?tmpDir=/home/bruno/odette/work&delay=5000";

	@Before
	public void setUp() throws Exception {
		log.info("********************************************************************************");
		log.info("Testing: " + getTestMethodName() + "(" + getClass().getName() + ")");
		log.info("********************************************************************************");

		log.debug("setUp test");
		if (!useJmx()) {
			disableJMX();
		} else {
			enableJMX();
		}

		context = createCamelContext();
		assertValidContext(context);

		// reduce default shutdown timeout to avoid waiting for 300 seconds
		context.getShutdownStrategy().setTimeout(120);

		template = context.createProducerTemplate();
		template.start();
		consumer = context.createConsumerTemplate();
		consumer.start();

		postProcessTest();

		if (isUseRouteBuilder()) {
			RouteBuilder[] builders = createRouteBuilders();
			for (RouteBuilder builder : builders) {
				log.debug("Using created route builder: " + builder);
				context.addRoutes(builder);
			}
			startCamelContext();
			log.debug("Routing Rules are: " + context.getRoutes());
		} else {
			log.debug("Using route builder from the created context: " + context);
		}
		log.debug("Routing Rules are: " + context.getRoutes());
	}

	public static class MyHandler implements OdetteHandler {
		public void acceptIncoming(IncomingFileResponse incomingFileResponse) {
			VirtualFile vf = incomingFileResponse.getVirtualFile();
			System.out.println(vf.getDatasetName());
			incomingFileResponse.acceptFile();
		}
	}

	@Test
	public void testFromFileToFtp() throws Exception {
		resultEndpoint.expectedMinimumMessageCount(1);
		resultEndpoint.assertIsSatisfied();
		resultEndpoint.await();
	}

	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() throws Exception {
				from("file:/home/bruno/odette/outbox")
					.to(oftpToUrl);

			from(oftpToUrl)
				.to("seda:a");

			from("seda:a")
				.choice()
					.when(header(OdetteEndpoint.ODETTE_DELIVERY_NOTIFICATION).isNotNull())
						.process(new Processor() {
							public void process(Exchange exchange) throws Exception {
								Thread.sleep(2000);
								System.out.println("DELIVERY NOTIFICATION ARRIVED");
								System.out.println(exchange.getIn().getBody());
							}
						})
					.otherwise()
						.to("file:/home/bruno/odette/inbox");
			}
		};
	}

}
