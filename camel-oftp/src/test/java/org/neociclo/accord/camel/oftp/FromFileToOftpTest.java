/**
 *  Neociclo Accord - Open Source B2B Integration Suite
 *  Copyright (C) 2005-2008 Neociclo, http://www.neociclo.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package org.neociclo.accord.camel.oftp;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class FromFileToOftpTest extends CamelTestSupport {

	private String oftpToUrl = "oftp://O0055PARTNERA@localhost:3305/?password=neociclo&originator=O0055PARTNERA&destination=O0055PARTNERB&datasetName=LOGFILE.RAR&stb=true";
	private String oftpFromUrl = "oftp://O0055PARTNERB@localhost:3305/?password=neociclo&regexPattern=LOGFILE.RAR&delay=300000&initialDelay=0&stb=true";

	public void testFromFileToFtp() throws Exception {
		MockEndpoint resultEndpoint = getMockEndpoint("mock:result");
		resultEndpoint.expectedMinimumMessageCount(1);
		resultEndpoint.assertIsSatisfied();

		// let some time pass to let the consumer etc. properly do its business
		// before closing
		Thread.sleep(1000);
	}

	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() throws Exception {
				from("file:src/test/data?noop=true").to(oftpToUrl);
				from(oftpFromUrl).to("mock:result");
			}
		};
	}

}
