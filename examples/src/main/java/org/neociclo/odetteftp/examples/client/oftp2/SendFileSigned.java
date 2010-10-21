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

import static org.neociclo.odetteftp.TransferMode.SENDER_ONLY;
import static org.neociclo.odetteftp.util.OdetteFtpSupport.createEnvelopedFile;
import static org.neociclo.odetteftp.util.OftpUtil.getFileSize;

import java.io.File;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLEngine;

import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.examples.MainSupport;
import org.neociclo.odetteftp.examples.support.SampleOftpSslContextFactory;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;
import org.neociclo.odetteftp.protocol.v20.DefaultEnvelopedVirtualFile;
import org.neociclo.odetteftp.protocol.v20.FileEnveloping;
import org.neociclo.odetteftp.protocol.v20.SecurityLevel;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.InOutSharedQueueOftpletFactory;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.support.PasswordHandler;
import org.neociclo.odetteftp.util.SecurityUtil;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class SendFileSigned {

	private static final String USER_KEYSTORE_FILE = "src/main/resources/keystores/client-bogus.p12";
	private static final String USER_KEYSTORE_PASSWORD = "neociclo";

	public static void main(String[] args) throws Exception {

		MainSupport ms = new MainSupport(SendFileSigned.class, args, "server", "port", "oid", "password",
				"payload");

		String host = ms.get(0);
		int port = Integer.parseInt(ms.get(1));
		String userCode = ms.get(2);
		String userPassword = ms.get(3);
		File payloadFile = new File(ms.get(4));

		File signedFile = File.createTempFile("signed-", "-" + payloadFile.getName(),
				payloadFile.getParentFile());

		OdetteFtpConfiguration conf = new OdetteFtpConfiguration();
		conf.setTransferMode(SENDER_ONLY);
		conf.setVersion(OdetteFtpVersion.OFTP_V20); // require OFTP2 connection

		MappedCallbackHandler securityCallbacks = new MappedCallbackHandler();
		securityCallbacks.addHandler(PasswordCallback.class,
				new PasswordHandler(userCode, userPassword));

		Queue<OdetteFtpObject> filesToSend = new ConcurrentLinkedQueue<OdetteFtpObject>();

		// construct enveloped virtual file object
		DefaultEnvelopedVirtualFile vf = new DefaultEnvelopedVirtualFile();
		vf.setFile(signedFile);
		vf.setEnvelopingFormat(FileEnveloping.CMS);
		vf.setSecurityLevel(SecurityLevel.SIGNED);
		vf.setCipherSuite(CipherSuite.TRIPLEDES_RSA_SHA1);

		// adding signature - load the private key and certificate used
		final KeyStore userKeystore = SecurityUtil.openKeyStore(new File(USER_KEYSTORE_FILE),
				USER_KEYSTORE_PASSWORD.toCharArray());
		X509Certificate userCert = SecurityUtil.getCertificateEntry(userKeystore);
		PrivateKey userPrivateKey = SecurityUtil.getPrivateKey(userKeystore, USER_KEYSTORE_PASSWORD.toCharArray());

		// create the signed file
		createEnvelopedFile(payloadFile, signedFile, vf, userCert, userPrivateKey, null);

		// set file size after output were properly created
		vf.setOriginalFileSize(getFileSize(payloadFile));
		vf.setSize(getFileSize(signedFile));

		filesToSend.offer(vf);

		OftpletFactory factory = new InOutSharedQueueOftpletFactory(conf, securityCallbacks, filesToSend, null, null);

		// create the client mode SSL engine
		SSLEngine sslEngine = SampleOftpSslContextFactory.getClientContext().createSSLEngine();
		sslEngine.setUseClientMode(true);
		sslEngine.setEnableSessionCreation(true);

		TcpClient oftp = new TcpClient(new InetSocketAddress(host, port), sslEngine, factory);

		oftp.connect(true);

		signedFile.delete();
	}

}
