package org.neociclo.accord.camel.oftp;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

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
 * /**
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class TestFromFileToOftp extends CamelTestSupport {

	@EndpointInject(uri = "mock:result")
	private MockEndpoint resultEndpoint;

	private String oftpFromUrl = "oftp://O0055SOFTMIDIA1:8169S412@200.244.109.85:6001?tmpDir=/tmp/odette&delay=5000";

	@Test(timeout = 60000)
	public void testFromFileToFtp() throws Exception {
		resultEndpoint.expectedMinimumMessageCount(1);
		resultEndpoint.assertIsSatisfied();
	}

	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() throws Exception {
				from(oftpFromUrl).to("mock:result");
			}
		};
	}

}
