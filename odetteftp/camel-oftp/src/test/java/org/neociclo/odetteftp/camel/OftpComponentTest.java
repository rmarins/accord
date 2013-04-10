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

import org.apache.camel.Endpoint;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.TransportType;
import org.neociclo.odetteftp.camel.OftpSettings;
import org.neociclo.odetteftp.camel.OftpServerEndpoint;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;


/**
 * @author Rafael Marins
 */
public class OftpComponentTest extends CamelTestSupport {

	@Test
	public void testCreateOftpServerEndpointWithRegularUsercode() throws Exception {
		OftpServerEndpoint endpoint = resolveMandatoryEndpoint("oftp-server://O0001WGRUS000987:secret@myhost:3305");
		OftpSettings config = endpoint.getSettings();
		assertEquals("getProtocol()", "oftp-server", config.getProtocol());
		assertEquals("getHost()", "myhost", config.getHost());
		assertEquals("getPort()", 3305, config.getPort());
		assertEquals("usercode", "O0001WGRUS000987", config.getUsercode());
		assertEquals("password", "secret", config.getPassword());
	}

	@Test
	public void testCreateOftpServerEndpointWithUsercodeWhitespace() throws Exception {

		String oid = "O0001WGRUS000987       RX";

		OftpServerEndpoint endpoint = resolveMandatoryEndpoint("oftp-server://" + escapeUriWhitespace(oid) +
				":secret@myhost:3305");
		OftpSettings config = endpoint.getSettings();
		assertEquals("getProtocol()", "oftp-server", config.getProtocol());
		assertEquals("getHost()", "myhost", config.getHost());
		assertEquals("getPort()", 3305, config.getPort());
		assertEquals("usercode", oid, config.getUsercode());
		assertEquals("password", "secret", config.getPassword());
	}

	@Test
	public void testOftpServerDefaults() throws Exception {
		OftpServerEndpoint endpoint = resolveMandatoryEndpoint("oftp-server://TheServerOID:secret@myhost");
		OftpSettings config = endpoint.getSettings();

		assertEquals("transport", TransportType.TCPIP, config.getTransport());
		assertEquals("ssl", false, config.isSsl());
		assertEquals("getPort()", 3305, config.getPort());
		assertNull("userData", config.getUserData());
		assertEquals("transferMode", TransferMode.BOTH, config.getTransferMode());
		assertEquals("debSize", 4096, config.getDebSize());
		assertEquals("windowSize", 64, config.getWindowSize());
		assertEquals("compression", false, config.isCompression());
		assertEquals("restart", false, config.isRestart());
		assertEquals("specialLogic", false, config.isSpecialLogic());
		assertEquals("secureAuth", false, config.isSecureAuth());
		assertEquals("cipherSuiteSel", CipherSuite.NO_CIPHER_SUITE_SELECTION, config.getCipherSuite());
		assertEquals("version", OdetteFtpVersion.OFTP_V20, config.getVersion());
		assertEquals("timeout", 90000, config.getTimeout());
		assertEquals("corePoolSize", 10, config.getCorePoolSize());
		assertEquals("maxPoolSize", 100, config.getMaxPoolSize());
	}

	@Test
	public void testCreateOftpServerEndpointWithParameters() throws Exception {
		OftpServerEndpoint endpoint = resolveMandatoryEndpoint("oftp-server://TheServerOID:secret@myhost/?" +
				"userData=DEV&debSize=1496&restart=true&cipherSuite=tripledesRsaSha1");
		OftpSettings config = endpoint.getSettings();

		assertEquals("userData", "DEV", config.getUserData());
		assertEquals("debSize", 1496, config.getDebSize());
		assertEquals("restart", true, config.isRestart());
		assertEquals("cipherSuite", CipherSuite.TRIPLEDES_RSA_SHA1, config.getCipherSuite());

	}

	@Test
	public void testCreateOftpServerEndpointWithTransportSpecified() throws Exception {
		OftpServerEndpoint endpoint = resolveMandatoryEndpoint("oftp-server://TheServerOID:secret@myhost/?transport=MoreDataBit");
		OftpSettings config = endpoint.getSettings();

		assertEquals("transport", TransportType.X25_MBGW, config.getTransport());
	}

	@Test
	public void testCreateEndpointWithIllegalTransport() throws Exception {

		try {
			resolveMandatoryEndpoint("oftp-server://TheServerOID:secret@5552323/?transport=ISDN");
			fail("It should have thrown an exception since ISDN is an invalid transport.");
		} catch (ResolveEndpointFailedException e) {
			assertTrue("Not information about the illegal transport value.", e.getMessage().endsWith("ISDN"));
		}
	}

	@Test
	public void testCreateEndpointWithAlternativePasswordParam() throws Exception {
		OftpServerEndpoint endpoint = resolveMandatoryEndpoint("oftp-server://TheServerOID@myhost/?password=secret");
		OftpSettings config = endpoint.getSettings();

		assertEquals("password", "secret", config.getPassword());
	}

	@Override
	protected OftpServerEndpoint resolveMandatoryEndpoint(String uri) {
		Endpoint endpoint = super.resolveMandatoryEndpoint(uri);
		return assertIsInstanceOf(OftpServerEndpoint.class, endpoint);
	}

	private String escapeUriWhitespace(String text) {
		return text.replaceAll("\\s", "%20");
	}
}
