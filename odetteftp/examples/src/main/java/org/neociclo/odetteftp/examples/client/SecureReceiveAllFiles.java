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
package org.neociclo.odetteftp.examples.client;

import static org.neociclo.odetteftp.TransferMode.RECEIVER_ONLY;
import static org.neociclo.odetteftp.protocol.AnswerReason.DUPLICATE_FILE;
import static org.neociclo.odetteftp.util.OdetteFtpSupport.getReplyDeliveryNotification;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLContext;

import org.neociclo.odetteftp.examples.MainSupport;
import org.neociclo.odetteftp.examples.support.SampleOftpSslContextFactory;
import org.neociclo.odetteftp.oftplet.EndFileResponse;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DefaultEndFileResponse;
import org.neociclo.odetteftp.protocol.DefaultStartFileResponse;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.InOutSharedQueueOftpletFactory;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.support.OftpletEventListenerAdapter;
import org.neociclo.odetteftp.support.PasswordHandler;

/**
 * @author Rafael Marins
 */
public class SecureReceiveAllFiles {

	public static void main(String[] args) throws Exception {

		MainSupport ms = new MainSupport(SecureReceiveAllFiles.class, args, "server", "port", "odetteid", "password",
				"directory");
		args = ms.args();

		String server = args[0];
		int port = Integer.parseInt(args[1]);
		String userCode = args[2];
		String userPassword = args[3];
		final File directory = new File(args[4]);

		OdetteFtpConfiguration conf = new OdetteFtpConfiguration();
		conf.setTransferMode(RECEIVER_ONLY);

		MappedCallbackHandler securityCallbacks = new MappedCallbackHandler();
		securityCallbacks.addHandler(PasswordCallback.class,
				new PasswordHandler(userCode, userPassword));

		final Queue<OdetteFtpObject> outgoingQueue = new ConcurrentLinkedQueue<OdetteFtpObject>();

		// create the client SSL context
		SSLContext sslContext = SampleOftpSslContextFactory.getClientContext();

		InOutSharedQueueOftpletFactory factory = new InOutSharedQueueOftpletFactory(conf, securityCallbacks,
				outgoingQueue, null, null);
		TcpClient oftp = new TcpClient(sslContext);
		oftp.setOftpletFactory(factory);

		// prepare the incoming handler
		factory.setEventListener(new OftpletEventListenerAdapter() {
			@Override
			public StartFileResponse acceptStartFile(VirtualFile incomingFile) {

				File saveToFile = null;

				saveToFile = new File(directory, incomingFile.getDatasetName());

				// handle duplicate file
				if (saveToFile.exists()) {
					DefaultStartFileResponse duplicateFile = DefaultStartFileResponse.negativeStartFileAnswer(DUPLICATE_FILE,
							"File already exist in local system.", true);
					return duplicateFile;
				}

				DefaultStartFileResponse acceptedFile = DefaultStartFileResponse.positiveStartFileAnswer(saveToFile);

				return acceptedFile;
			}

			@Override
			public void onReceiveFileStart(VirtualFile virtualFile, long answerCount) {
				System.out.println("Begin receiving file: " + virtualFile);
			}

			@Override
			public EndFileResponse onReceiveFileEnd(VirtualFile virtualFile, long recordCount, long unitCount) {

				// reply with EERP (positive delivery notification)
				DeliveryNotification notif = getReplyDeliveryNotification(virtualFile);
				outgoingQueue.offer(notif);

				System.out.println("Receive file completed: " + virtualFile);

				// send the EERP back - request change direction (true)
				return DefaultEndFileResponse.positiveEndFileAnswer();
			}

		});

		oftp.connect(server, port, true);

	}

}
