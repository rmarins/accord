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
package org.neociclo.odetteftp.examples.client.oftp2;

import static org.neociclo.odetteftp.TransferMode.RECEIVER_ONLY;
import static org.neociclo.odetteftp.protocol.AnswerReason.DUPLICATE_FILE;
import static org.neociclo.odetteftp.protocol.v20.SecurityLevel.ENCRYPTED;
import static org.neociclo.odetteftp.protocol.v20.SecurityLevel.ENCRYPTED_AND_SIGNED;
import static org.neociclo.odetteftp.protocol.v20.SecurityLevel.SIGNED;
import static org.neociclo.odetteftp.util.OdetteFtpSupport.getReplyDeliveryNotification;
import static org.neociclo.odetteftp.util.OdetteFtpSupport.parseEnvelopedFile;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.examples.MainSupport;
import org.neociclo.odetteftp.oftplet.EndFileResponse;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DefaultEndFileResponse;
import org.neociclo.odetteftp.protocol.DefaultStartFileResponse;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.protocol.v20.DefaultSignedDeliveryNotification;
import org.neociclo.odetteftp.protocol.v20.EnvelopedVirtualFile;
import org.neociclo.odetteftp.protocol.v20.FileEnveloping;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.InOutSharedQueueOftpletFactory;
import org.neociclo.odetteftp.support.OftpletEventListenerAdapter;
import org.neociclo.odetteftp.support.SessionConfig;
import org.neociclo.odetteftp.util.EnvelopingException;
import org.neociclo.odetteftp.util.EnvelopingUtil;
import org.neociclo.odetteftp.util.SecurityUtil;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class ReceiveEnvelopedFiles {

	private static final String USER_KEYSTORE_FILE = "src/main/resources/keystores/client-bogus.p12";
	private static final String USER_KEYSTORE_PASSWORD = "neociclo";

	private static final String PARTNER_CERTIFICATE_FILE = "src/main/resources/certificates/o0055partnera-public.cer";

	public static void main(String[] args) throws Exception {

		MainSupport ms = new MainSupport(ReceiveEnvelopedFiles.class, args, "server", "port", "odetteid", "password",
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
		// require an OFTP2 connection
		conf.setVersion(OdetteFtpVersion.OFTP_V20);

		// add signature: pre-load user's private key and certificate
		KeyStore userKs = SecurityUtil.openKeyStore(new File(USER_KEYSTORE_FILE),
				USER_KEYSTORE_PASSWORD.toCharArray());
		final X509Certificate userCert = SecurityUtil.getCertificateEntry(userKs);
		final PrivateKey userPrivateKey = SecurityUtil.getPrivateKey(userKs, USER_KEYSTORE_PASSWORD.toCharArray());

		final Queue<OdetteFtpObject> outgoingQueue = new ConcurrentLinkedQueue<OdetteFtpObject>();

		InOutSharedQueueOftpletFactory factory = new InOutSharedQueueOftpletFactory(conf, outgoingQueue, null, null);
		TcpClient oftp = new TcpClient(server, port, factory);

		// prepare the incoming handler
		factory.setEventListener(new OftpletEventListenerAdapter() {
			@Override
			public StartFileResponse acceptStartFile(VirtualFile incomingFile) {

				File saveToFile = null;

				saveToFile = new File(directory, incomingFile.getDatasetName());

				// handle duplicate file
				if (saveToFile.exists()) {
					DefaultStartFileResponse duplicateFile = DefaultStartFileResponse.negativeAnswer(DUPLICATE_FILE,
							"File already exist in local system.", true);
					return duplicateFile;
				}

				DefaultStartFileResponse acceptedFile = DefaultStartFileResponse.positiveAnswer(saveToFile);

				return acceptedFile;
			}

			@Override
			public void onReceiveFileStart(VirtualFile virtualFile, long answerCount) {
				System.out.println("Begin receiving file: " + virtualFile);
			}

			@Override
			public EndFileResponse onReceiveFileEnd(VirtualFile argVirtualFile, long recordCount, long unitCount) {

				System.out.println("Receive file completed: " + argVirtualFile);

				EnvelopedVirtualFile vf = (EnvelopedVirtualFile) argVirtualFile;

				/*
				 * UN-WRAP the original payload from the received file
				 * (encrypted, signed and/or compressed)
				 */

				if (vf.getEnvelopingFormat() != FileEnveloping.NO_ENVELOPE) {

					File originalPayload = null;

					try {
						originalPayload = File.createTempFile(vf.getDatasetName() + "-", ".original", directory);
					} catch (IOException e) {

						// XXX you can handle the unwrapping later if matters

						System.err.println("Cannot create unenveloped temp file in: " + directory);
						System.err.println();
						e.printStackTrace();

						return DefaultEndFileResponse.positiveAnswer();
					}

					X509Certificate userCert = null;
					PrivateKey userPrivateKey = null;
					X509Certificate partnerCert = null;

					boolean isSigned = (vf.getSecurityLevel() == SIGNED || vf.getSecurityLevel() == ENCRYPTED_AND_SIGNED);
					boolean isEncrypted = (vf.getSecurityLevel() == ENCRYPTED || vf.getSecurityLevel() == ENCRYPTED_AND_SIGNED);

					if (isSigned) {
						// provide the assigned partner's certificate to verify
						// the signature in the received file
						try {
							partnerCert = SecurityUtil.openCertificate(new File(PARTNER_CERTIFICATE_FILE));
						} catch (Exception e) {

							/*
							 * caught on FileNotFoundException,
							 * CertificateException or NoSuchProviderException
							 */
							
							System.err.println("Cannot parse enveloped file. Error loading partner certificate: "
									+ e.getMessage());
							System.err.println();
							e.printStackTrace();

							return DefaultEndFileResponse.positiveAnswer();
						}
					}

					if (isEncrypted) {
						// provide the user certificate and private key (loaded)
						// to decrypt the received file
						
						try {

							KeyStore userKeystore = SecurityUtil.openKeyStore(new File(USER_KEYSTORE_FILE),
									USER_KEYSTORE_PASSWORD.toCharArray());
							userCert = SecurityUtil.getCertificateEntry(userKeystore);
							userPrivateKey = SecurityUtil.getPrivateKey(userKeystore, USER_KEYSTORE_PASSWORD.toCharArray());

						} catch (Exception e) {

							/*
							 * caught on KeyStoreException, IOException,
							 * NoSuchProviderException, CertificateException,
							 * NoSuchAlgorithmException and
							 * UnrecoverableKeyException
							 */

							System.err.println("Cannot parse enveloped file. Error loading user private key or certificate: "
									+ e.getMessage());
							System.err.println();
							e.printStackTrace();

							return DefaultEndFileResponse.positiveAnswer();
						}
					}

					try {

						// decrypt, decompress and/or check-remove signature to
						// the output file (originalPayload)

						parseEnvelopedFile(vf.getFile(), originalPayload, vf, userCert, userPrivateKey, partnerCert);

					} catch (EnvelopingException e) {

						// XXX good place to reply with a Negative End Response (NERP)

						System.err.println("Cannot parse enveloped file. Enveloping error: " + e.getMessage());
						System.err.println();
						e.printStackTrace();

						return DefaultEndFileResponse.positiveAnswer();
					}
					
				}

				// reply with a successful End-to-End Response (EERP)
				DefaultSignedDeliveryNotification notif = (DefaultSignedDeliveryNotification) getReplyDeliveryNotification(vf);

				// ADD SIGNATURE to the EERP when required
				if (vf.isSignedNotificationRequest()) {
					try {
						EnvelopingUtil.addNotifSignature(notif, vf.getCipherSuite(), userCert, userPrivateKey);
					} catch (Exception e) {
						System.err.println("Cannot reply with a Signed EERP. Add signature failed: " + vf);
						System.err.println();
						e.printStackTrace();
					}
				}

				outgoingQueue.offer(notif);

				// to send the EERP back, request change direction (true)
				return DefaultEndFileResponse.positiveAnswer();
			}

		});

		// instant the connection is performed and then executed all steps above
		oftp.connect(true);

	}

}
