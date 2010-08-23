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

import org.neociclo.odetteftp.examples.MainSupport;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.DefaultOftpletFactory;
import org.neociclo.odetteftp.support.SessionConfig;

/**
 * @author Rafael Marins
 * @version $Rev$
 */
public class ConnectAndDisconnect {

	public static void main(String[] args) throws Exception {

		MainSupport ms = new MainSupport(ConnectAndDisconnect.class, args, "server", "port", "odetteid", "password");

		String server = ms.get(0);
		int port = Integer.parseInt(ms.get(1));
		String odetteid = ms.get(2);
		String password = ms.get(3);

		SessionConfig conf = new SessionConfig();
		conf.setUserCode(odetteid);
		conf.setUserPassword(password);

		OftpletFactory factory = new DefaultOftpletFactory(conf);
		TcpClient oftp = new TcpClient(server, port, factory);

		oftp.connect(true);

	}

}
