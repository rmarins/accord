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
package org.neociclo.odetteftp.examples.server;

import static org.neociclo.odetteftp.examples.server.SimpleServerHelper.getUserConfigFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;

import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.PasswordAuthenticationCallback;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.service.TcpServer;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.support.OftpletEventListenerAdapter;
import org.neociclo.odetteftp.support.PasswordHandler;
import org.neociclo.odetteftp.support.PropertiesBasedConfiguration;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class SimpleServer {

	private static final int SERVER_PORT = 13305;

	private static final File SERVER_DIR = new File(".", "simpleserver-data");

	public static void main(String[] args) throws Exception {

		InetSocketAddress localAddress = new InetSocketAddress(SERVER_PORT);

		OdetteFtpConfiguration config = createInitialServerConfig();

		MappedCallbackHandler serverSecurityHandler = new MappedCallbackHandler();

		//
		// add server password authentication handler based on the users
		// properties file
		//
		serverSecurityHandler.addHandler(PasswordAuthenticationCallback.class,
				new SimpleServerAuthenticationHandler(SERVER_DIR));

		//
		// add password callback which tells the library to reply with server
		// side identification and password
		//
		serverSecurityHandler.addHandler(PasswordCallback.class,
				new PasswordHandler("O0055MYSERVERID", "MYPASSWD"));

		SimpleServerOftpletFactory factory = new SimpleServerOftpletFactory(SERVER_DIR, config, serverSecurityHandler,
				new OftpletEventListenerAdapter() {

			@Override
			public void configure(OdetteFtpSession session) {
				// setup custom parameters specific to this user configuration
				String userCode = session.getUserCode();
				File configFile = getUserConfigFile(SERVER_DIR, userCode);
				PropertiesBasedConfiguration customConfig = new PropertiesBasedConfiguration();

				try {
					customConfig.load(new FileInputStream(configFile));
					customConfig.setup(session);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		TcpServer server = new TcpServer(localAddress, factory);

		server.start();

		System.out.println("Press Ctrl+C to stop.");
	}

	private static OdetteFtpConfiguration createInitialServerConfig() {
		OdetteFtpConfiguration c = new OdetteFtpConfiguration();

		c.setTransferMode(TransferMode.BOTH);
		c.setVersion(OdetteFtpVersion.OFTP_V14);
		c.setDataExchangeBufferSize(4096);
		c.setWindowSize(64);

		c.setUseSecureAuthentication(false);
		c.setCipherSuiteSelection(CipherSuite.NO_CIPHER_SUITE_SELECTION);

		return c;
	}

	public static URL getResource(String name) {
        return Thread.currentThread().getContextClassLoader().getResource(name);
    }

    public static File getResourceFile(String name) throws URISyntaxException {
        return new File(getResource(name).toURI());
    }

}
