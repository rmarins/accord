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

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.examples.MainSupport;
import org.neociclo.odetteftp.examples.support.DefaultOftpletFactory;
import org.neociclo.odetteftp.examples.support.SampleOftpSslContextFactory;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;
import org.neociclo.odetteftp.security.AuthenticationChallengeCallback;
import org.neociclo.odetteftp.security.EncryptAuthenticationChallengeCallback;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.OneToOneHandler;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.support.PasswordHandler;
import org.neociclo.odetteftp.util.EnvelopingUtil;
import org.neociclo.odetteftp.util.SecurityUtil;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class PerformSecureAuthentication {

	private static final String USER_KEYSTORE_FILE = "src/main/resources/keystores/client-bogus.p12";
	private static final String USER_KEYSTORE_PASSWORD = "neociclo";

	private static final String PARTNER_CERTIFICATE_FILE = "src/main/resources/certificates/o0055partnera-public.cer";

	public static void main(String[] args) throws Exception {

		MainSupport ms = new MainSupport(PerformSecureAuthentication.class, args, "server", "port", "oid", "password");

		String server = ms.get(0);
		int port = Integer.parseInt(ms.get(1));
		String userCode = ms.get(2);
		String userPassword = ms.get(3);

		OdetteFtpConfiguration conf = new OdetteFtpConfiguration();
		conf.setTransferMode(TransferMode.SENDER_ONLY);
		conf.setVersion(OdetteFtpVersion.OFTP_V20); // require OFTP2 connection

		// setup secure authentication options
		conf.setUseSecureAuthentication(true);
		final KeyStore userKeystore = SecurityUtil.openKeyStore(new File(USER_KEYSTORE_FILE),
				USER_KEYSTORE_PASSWORD.toCharArray());

		MappedCallbackHandler secureAuthenticationHandler = new MappedCallbackHandler();

		/*
		 * Add password authentication.
		 */
		secureAuthenticationHandler.addHandler(PasswordCallback.class,
				new PasswordHandler(userCode, userPassword));

		/*
		 * The received authentication challenged is encrypted with user's
		 * associated public certificate and must be decrypted and sent back.
		 * It's done using the AuthenticatioChallengeCallback.
		 * 
		 * For more information, see the Secure Authentication protocol sequence
		 * (section 4.2.4) in the protocol specification RFC5024.
		 */
		secureAuthenticationHandler.addHandler(AuthenticationChallengeCallback.class,
				new OneToOneHandler<AuthenticationChallengeCallback>() {
					public void handle(AuthenticationChallengeCallback cb) throws IOException {

						try {
							// load user's certificate and private key
							X509Certificate cert = SecurityUtil.getCertificateEntry(userKeystore);
							PrivateKey key = SecurityUtil.getPrivateKey(userKeystore,
									USER_KEYSTORE_PASSWORD.toCharArray());

							// decrypt the authentication challenge
							byte[] challengeResponse = EnvelopingUtil.parseEnvelopedData(cb.getEncodedChallenge(),
									cert, key);

							// indicate the challenge response via callback
							cb.setChallenge(challengeResponse);

						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});

		/*
		 * The secure authentication is completed when the Initiator sends an
		 * challenge encrypted with the remote peer's public certificate.
		 * 
		 * For more information, see the Secure Authentication protocol sequence
		 * (section 4.2.4) in the protocol specification RFC5024.
		 */
		secureAuthenticationHandler.addHandler(EncryptAuthenticationChallengeCallback.class,
				new OneToOneHandler<EncryptAuthenticationChallengeCallback>() {
					public void handle(EncryptAuthenticationChallengeCallback cb) throws IOException {

						try {
							X509Certificate cert = SecurityUtil.openCertificate(new File(PARTNER_CERTIFICATE_FILE));
							CipherSuite cipherSel = cb.getSession().getCipherSuiteSelection();
							byte[] encryptedChallenge = EnvelopingUtil.createEnvelopedData(cb.getChallenge(), cipherSel, cert);
							cb.setEncodedChallenge(encryptedChallenge);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

		OftpletFactory factory = new DefaultOftpletFactory(conf, secureAuthenticationHandler);

		// create the client mode SSL context
		SSLContext sslContext = SampleOftpSslContextFactory.getClientContext();

		TcpClient oftp = new TcpClient(new InetSocketAddress(server, port), sslContext, factory);

		oftp.connect(true);

	}

}
