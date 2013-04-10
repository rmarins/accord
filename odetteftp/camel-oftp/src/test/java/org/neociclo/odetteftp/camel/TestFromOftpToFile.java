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
 */
package org.neociclo.odetteftp.camel;

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

				from("oftp://O0055SOFTMIDIA1:8169S412@200.244.109.85:6001")
				.to("file:odette/inbox").to("mock:result");
			}
		};
	}

}
