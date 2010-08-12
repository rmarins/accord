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

import static org.neociclo.odetteftp.TransferMode.*;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.neociclo.odetteftp.examples.support.InOutSharedQueueOftpletFactory;
import org.neociclo.odetteftp.examples.support.SessionConfig;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.service.TcpClient;

/**
 * @author Rafael Marins
 * @version $Rev$
 */
public class SendFile {

	public static void main(String[] args) throws Exception {

		if (args.length != 5) {
			System.err.println("Incorrect number of arguments.");
			System.err.println();

			printUsage();
			System.exit(-1);
		}

		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String usercode = args[2];
		String password = args[3];
		File payload = new File(args[4]);

		SessionConfig conf = new SessionConfig();
		conf.setUserCode(usercode);
		conf.setUserPassword(password);
		conf.setTransferMode(SENDER_ONLY);

		Queue<OdetteFtpObject> filesToSend = new ConcurrentLinkedQueue<OdetteFtpObject>();

        DefaultVirtualFile vf = new DefaultVirtualFile();
        vf.setFile(payload);

		filesToSend.offer(vf);

		OftpletFactory factory = new InOutSharedQueueOftpletFactory(conf, filesToSend, null, null);
		TcpClient oftp = new TcpClient(host, port, factory);

		oftp.connect(true);

	}

	private static void printUsage() {
		System.out.println("SendFile <host> <port> <user-code> <user-password> <file>");
		System.out.println();
	}

}
