package org.neociclo.accord.camel.oftp;

import java.net.URI;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.neociclo.accord.camel.odette.OdetteComponent;
import org.neociclo.accord.camel.odette.OdetteConfiguration;
import org.neociclo.accord.camel.odette.OdetteEndpoint;

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
 * 21:1org.neociclo.accord.camel.oftpe org.neociclo.accord.camel.oftp;
 * org.neociclo.accord.camel.oftporg.apache.camel.EndpointInject; import
 * org.apache.camel.builder.RouteBuilder; import
 * org.apache.camel.component.mock.MockEndpoint; import
 * org.apache.camel.test.junit4.CamelTestSupport; import org.junit.Test; import
 * org.neociclo.accord.camel.odette.OdetteComponent; import
 * org.neociclo.accord.camel.odette.OdetteConfiguration; import
 * org.neociclo.accord.camel.odette.OdetteEndpoint;
 * 
 * /**
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class TestFromFileToOftp extends CamelTestSupport {

	@EndpointInject(uri = "mock:result")
	private MockEndpoint resultEndpoint;

	private String oftpFromUrl = "oftp://O0055SOFTMIDIA1:8169S412@200.244.109.85:6001";

	@Test(timeout = 60000)
	public void testFromFileToFtp() throws Exception {
		resultEndpoint.expectedMinimumMessageCount(1);
		resultEndpoint.assertIsSatisfied();
	}

	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() throws Exception {
				OdetteConfiguration configuration = new OdetteConfiguration();
				configuration.configure(new URI(oftpFromUrl));
				OdetteComponent component = new OdetteComponent();
				component.setCamelContext(getContext());
				OdetteEndpoint endpoint = new OdetteEndpoint(oftpFromUrl, component, configuration);
				endpoint.setCamelContext(getContext());
				from(endpoint).to("mock:result");
			}
		};
	}

}
