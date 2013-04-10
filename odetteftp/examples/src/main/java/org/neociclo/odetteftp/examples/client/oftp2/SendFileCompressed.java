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
package org.neociclo.odetteftp.examples.client.oftp2;

import static org.neociclo.odetteftp.TransferMode.SENDER_ONLY;
import static org.neociclo.odetteftp.util.OdetteFtpSupport.createEnvelopedFile;
import static org.neociclo.odetteftp.util.OftpUtil.getFileSize;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLContext;

import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.examples.MainSupport;
import org.neociclo.odetteftp.examples.support.SampleOftpSslContextFactory;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.v20.DefaultEnvelopedVirtualFile;
import org.neociclo.odetteftp.protocol.v20.FileCompression;
import org.neociclo.odetteftp.protocol.v20.FileEnveloping;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.InOutSharedQueueOftpletFactory;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.support.PasswordHandler;

/**
 * @author Rafael Marins
 */
public class SendFileCompressed {

	public static void main(String[] args) throws Exception {

		MainSupport ms = new MainSupport(SendFileCompressed.class, args, "server", "port", "oid", "password",
				"payload");

		String host = ms.get(0);
		int port = Integer.parseInt(ms.get(1));
		String userCode = ms.get(2);
		String userPassword = ms.get(3);
		File payloadFile = new File(ms.get(4));

		File compressedFile = File.createTempFile("compressed-", "-" + payloadFile.getName(),
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
		vf.setFile(compressedFile);
		vf.setEnvelopingFormat(FileEnveloping.CMS);
		vf.setCompressionAlgorithm(FileCompression.ZLIB);

		// create the compressed file
		createEnvelopedFile(payloadFile, compressedFile, vf);

		// set file size after compression
		vf.setOriginalFileSize(getFileSize(payloadFile));
		vf.setSize(getFileSize(compressedFile));

		filesToSend.offer(vf);

		OftpletFactory factory = new InOutSharedQueueOftpletFactory(conf, securityCallbacks, filesToSend, null, null);

		// create the client mode SSL context
		SSLContext sslContext = SampleOftpSslContextFactory.getClientContext();

		TcpClient oftp = new TcpClient(sslContext);
		oftp.setOftpletFactory(factory);

		oftp.connect(new InetSocketAddress(host, port), true);

		compressedFile.delete();
	}

}
