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
package org.neociclo.odetteftp.examples.client;

import java.net.InetSocketAddress;

import javax.net.ssl.SSLEngine;

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
		SSLEngine sslEngine = SampleOftpSslContextFactory.getClientContext().createSSLEngine();
		sslEngine.setUseClientMode(true);
		sslEngine.setEnableSessionCreation(true);

		TcpClient oftp = new TcpClient(new InetSocketAddress(server, port), sslEngine, factory);

		oftp.connect(true);

	}

}
