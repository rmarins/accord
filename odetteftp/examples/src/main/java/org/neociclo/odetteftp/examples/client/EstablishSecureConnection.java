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
package org.neociclo.odetteftp.examples.client;

import java.net.InetSocketAddress;

import javax.net.ssl.SSLContext;

import org.neociclo.odetteftp.examples.MainSupport;
import org.neociclo.odetteftp.examples.support.DefaultOftpletFactory;
import org.neociclo.odetteftp.examples.support.SampleOftpSslContextFactory;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.PasswordHandler;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class EstablishSecureConnection {

	public static void main(String[] args) throws Exception {

		MainSupport ms = new MainSupport(EstablishSecureConnection.class, args, "server", "port", "oid", "password");

		String server = ms.get(0);
		int port = Integer.parseInt(ms.get(1));
		String userCode = ms.get(2);
		String userPassword = ms.get(3);

		MappedCallbackHandler securityCallbacks = new MappedCallbackHandler();
		securityCallbacks.addHandler(PasswordCallback.class,
				new PasswordHandler(userCode, userPassword));

		OftpletFactory factory = new DefaultOftpletFactory(securityCallbacks);

		// create the client mode SSL engine
		SSLContext sslContext = SampleOftpSslContextFactory.getClientContext();

		TcpClient oftp = new TcpClient(sslContext);
		oftp.setOftpletFactory(factory);

		oftp.connect(new InetSocketAddress(server, port), true);

	}

}
