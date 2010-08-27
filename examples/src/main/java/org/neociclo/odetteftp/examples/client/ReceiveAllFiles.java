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

import static org.neociclo.odetteftp.TransferMode.*;
import static org.neociclo.odetteftp.protocol.AnswerReason.*;
import static org.neociclo.odetteftp.util.OdetteFtpSupport.getReplyDeliveryNotification;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.neociclo.odetteftp.examples.MainSupport;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DefaultStartFileResponse;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.InOutOftpletEventListenerAdapter;
import org.neociclo.odetteftp.support.InOutSharedQueueOftpletFactory;
import org.neociclo.odetteftp.support.SessionConfig;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class ReceiveAllFiles {

	public static void main(String[] args) throws Exception {

		MainSupport ms = new MainSupport(ReceiveAllFiles.class, args, "server", "port", "odetteid", "password",
				"directory");
		args = ms.args();

		String server = args[0];
		int port = Integer.parseInt(args[1]);
		String odetteid = args[2];
		String password = args[3];
		final File directory = new File(args[4]);

		SessionConfig conf = new SessionConfig();
		conf.setUserCode(odetteid);
		conf.setUserPassword(password);

		conf.setTransferMode(RECEIVER_ONLY);

		final Queue<OdetteFtpObject> outgoingQueue = new ConcurrentLinkedQueue<OdetteFtpObject>();

		InOutSharedQueueOftpletFactory factory = new InOutSharedQueueOftpletFactory(conf, outgoingQueue, null, null);
		TcpClient oftp = new TcpClient(server, port, factory);

		// prepare the incoming handler
		factory.setEventListener(new InOutOftpletEventListenerAdapter() {
			@Override
			public StartFileResponse acceptStartFile(VirtualFile incomingFile) {

				File saveToFile = null;

				saveToFile = new File(directory, incomingFile.getDatasetName());

				// handle duplicate file
				if (saveToFile.exists()) {
					DefaultStartFileResponse duplicateFile = new DefaultStartFileResponse(false, DUPLICATE_FILE,
							"File already exist in local system.", true);
					return duplicateFile;
				}

				DefaultStartFileResponse acceptedFile = new DefaultStartFileResponse(true);
				acceptedFile.setFile(saveToFile);

				return acceptedFile;
			}

			@Override
			public void onReceiveFileStart(VirtualFile virtualFile, long answerCount) {
				System.out.println("Begin receiving file: " + virtualFile);
			}

			@Override
			public boolean onReceiveFileEnd(VirtualFile virtualFile, long recordCount, long unitCount) {

				// reply with EERP (positive delivery notification)
				DeliveryNotification notif = getReplyDeliveryNotification(virtualFile);
				outgoingQueue.offer(notif);

				System.out.println("Receive file completed: " + virtualFile);

				// send the EERP back - request change direction (true)
				return true;
			}

		});

		oftp.connect(true);

	}

}
