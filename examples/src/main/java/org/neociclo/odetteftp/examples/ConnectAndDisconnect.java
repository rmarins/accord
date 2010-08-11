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
package org.neociclo.odetteftp.examples;

import org.neociclo.odetteftp.examples.support.DefaultOftpletFactory;
import org.neociclo.odetteftp.examples.support.SessionConfig;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.service.TcpClient;

/**
 * @author Rafael Marins
 * @version $Rev$
 */
public class ConnectAndDisconnect {

	public static void main(String[] args) throws Exception {

		if (args.length != 4) {
			System.err.println("Incorrect number of arguments.");
			System.err.println();

			printUsage();
			System.exit(-1);
		}

		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String usercode = args[2];
		String password = args[3];

		SessionConfig conf = new SessionConfig();
		conf.setUserCode(usercode);
		conf.setUserPassword(password);

		OftpletFactory factory = new DefaultOftpletFactory(conf);
		TcpClient oftp = new TcpClient(host, port, factory);

		oftp.connect(true);

	}

	private static void printUsage() {
		System.out.println("ConnectAndDisconnect <host> <port> <user-code> <user-password>");
		System.out.println();
	}
}
