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
package org.neociclo.odetteftp.examples.server;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;

import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.examples.support.SessionFinalizationListener;
import org.neociclo.odetteftp.examples.support.UserPropertiesAutheticationHandler;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.PasswordAuthenticationCallback;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.service.TcpServer;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.support.PasswordHandler;

/**
 * @author Rafael Marins
 */
public class AuthenticateUsersServer {

	private static final String USER_PROPERTIES_RES = "mailboxes.properties";

	private static final int SERVER_PORT = 13305;

	public static void main(String[] args) throws Exception {

		InetSocketAddress localAddress = new InetSocketAddress(SERVER_PORT);

		OdetteFtpConfiguration config = createInitialServerConfig();

		MappedCallbackHandler serverSecurityHandler = new MappedCallbackHandler();

		//
		// add server password authentication handler based on the users
		// properties file
		//
		File userProperties = getResourceFile(USER_PROPERTIES_RES);
		serverSecurityHandler.addHandler(PasswordAuthenticationCallback.class,
				new UserPropertiesAutheticationHandler(userProperties));

		//
		// add password callback which tells the library to reply with server
		// side identification and password
		//
		serverSecurityHandler.addHandler(PasswordCallback.class,
				new PasswordHandler("O0055MYSERVERID", "MYPASSWD"));

		SessionFinalizationListener sessionFinalizer = new SessionFinalizationListener(1);

		AuthenticateUsersServerOftpletFactory factory = new AuthenticateUsersServerOftpletFactory(config, serverSecurityHandler, sessionFinalizer);
		TcpServer server = new TcpServer(localAddress, factory);

		server.start();

		sessionFinalizer.waitFinalization();
		server.stop();

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
