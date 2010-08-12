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
import java.io.IOException;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.neociclo.odetteftp.examples.support.InOutOftpletEventListenerAdapter;
import org.neociclo.odetteftp.examples.support.InOutSharedQueueOftpletFactory;
import org.neociclo.odetteftp.examples.support.SessionConfig;
import org.neociclo.odetteftp.oftplet.AnswerReasonInfo;
import org.neociclo.odetteftp.protocol.AnswerReason;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.util.IoUtil;

/**
 * @author Rafael Marins
 * @version $Rev$
 */
public class HandlingSendFileEvents {

	public static void main(String[] args) throws Exception {

		MainSupport ms = new MainSupport(HandlingSendFileEvents.class, args, "server", "port", "odetteid", "password",
				"payload");
		args = ms.args();

		String server = args[0];
		int port = Integer.parseInt(args[1]);
		String odetteid = args[2];
		String password = args[3];
		final File payload = new File(args[4]);

		SessionConfig conf = new SessionConfig();
		conf.setUserCode(odetteid);
		conf.setUserPassword(password);
		conf.setTransferMode(SENDER_ONLY);

		final Queue<OdetteFtpObject> filesToSend = new ConcurrentLinkedQueue<OdetteFtpObject>();

		DefaultVirtualFile vf = new DefaultVirtualFile();
		vf.setFile(payload);

		filesToSend.offer(vf);

		InOutSharedQueueOftpletFactory factory = new InOutSharedQueueOftpletFactory(
				conf, filesToSend, null, null);
		TcpClient oftp = new TcpClient(server, port, factory);

		factory.setEventListener(new InOutOftpletEventListenerAdapter() {

			// handle sent file end-to-end response
			@Override
			public void onNotificationReceived(DeliveryNotification notif) {
				System.out.println("Received EERP: " + notif);
			}

			@Override
			public void onSendFileStart(VirtualFile virtualFile,
					long answerCount) {

				File sourceFile = virtualFile.getFile();
				File tempFile = null;

				try {
					tempFile = File.createTempFile("oftp-", ".sent");
					IoUtil.copy(sourceFile, tempFile);

					System.out
							.println("Copying payload in temporary before sending: "
									+ tempFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			@Override
			public void onSendFileEnd(VirtualFile virtualFile) {
				File tempFile = virtualFile.getFile();
				IoUtil.delete(tempFile);
				System.out.println("Deleting temporary payload file: "
						+ tempFile);
			}

			// send file errors
			@Override
			public void onSendFileError(VirtualFile virtualFile,
					AnswerReasonInfo reason, boolean retryLater) {

				// re-send file with different name
				if (reason.getAnswerReason() == AnswerReason.DUPLICATE_FILE) {
					DefaultVirtualFile renamedFile = new DefaultVirtualFile();
					renamedFile.setFile(payload);
					renamedFile.setDatasetName(payload.getName() + "-"
							+ (new Random()).nextInt(100));
					filesToSend.add(renamedFile);
				} else {
					System.out.println("Send File Error: " + reason);
				}
			}

		});

		// perform connection and transfer
		oftp.connect(true);

	}

}
