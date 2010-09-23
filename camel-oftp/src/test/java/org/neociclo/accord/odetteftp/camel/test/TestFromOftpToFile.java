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
package org.neociclo.accord.odetteftp.camel.test;

import org.apache.camel.EndpointInject;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;

public class TestFromOftpToFile extends CamelTestSupport {

	@EndpointInject(uri = "mock:result")
	private MockEndpoint resultEndpoint;

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
		context.getShutdownStrategy().setTimeout(60);

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

	@Test
	public void testFromFileToFtp() throws Exception {
		resultEndpoint.expectedMinimumMessageCount(1);
		resultEndpoint.assertIsSatisfied();
		resultEndpoint.await();
	}

	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() throws Exception {
				errorHandler(loggingErrorHandler().level(LoggingLevel.TRACE));

				from("oftp://O0055SOFTMIDIA1:8169S412@200.244.109.85:6001?workpath=odette/work")
				.to("file:odette/inbox").to("mock:result");
			}
		};
	}

}
