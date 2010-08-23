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

import static org.neociclo.odetteftp.TransferMode.*;
import static org.neociclo.odetteftp.util.OdetteFtpSupport.*;
import static org.neociclo.odetteftp.util.OftpUtil.*;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLEngine;

import org.neociclo.odetteftp.examples.MainSupport;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.v20.DefaultEnvelopedVirtualFile;
import org.neociclo.odetteftp.protocol.v20.FileCompression;
import org.neociclo.odetteftp.protocol.v20.FileEnveloping;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.InOutSharedQueueOftpletFactory;
import org.neociclo.odetteftp.support.SampleOftpSslContextFactory;
import org.neociclo.odetteftp.support.SessionConfig;
import org.neociclo.odetteftp.util.OftpUtil;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class SendFileCompressed {

	public static void main(String[] args) throws Exception {

		MainSupport ms = new MainSupport(SendFileCompressed.class, args, "server", "port", "oid", "password",
				"payload");
//		args = ms.args();

		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String usercode = args[2];
		String password = args[3];
		File payloadFile = new File(args[4]);

		File compressedFile = File.createTempFile("compressed-", "-" + payloadFile.getName(),
				payloadFile.getParentFile());

		SessionConfig conf = new SessionConfig();
		conf.setUserCode(usercode);
		conf.setUserPassword(password);
		conf.setTransferMode(SENDER_ONLY);

		Queue<OdetteFtpObject> filesToSend = new ConcurrentLinkedQueue<OdetteFtpObject>();

		// construct enveloped virtual file object
		DefaultEnvelopedVirtualFile vf = new DefaultEnvelopedVirtualFile();
		vf.setFile(compressedFile);
		vf.setEnvelopingFormat(FileEnveloping.CMS);
		vf.setCompressionAlgorithm(FileCompression.ZLIB);

		vf.setOriginalFileSize(getFileSize(payloadFile));
		vf.setSize(getFileSize(compressedFile));

		// create the compressed file
		createEnvelopedFile(payloadFile, compressedFile, vf);

		filesToSend.offer(vf);

		OftpletFactory factory = new InOutSharedQueueOftpletFactory(conf, filesToSend, null, null);

		// create the client mode SSL engine
		SSLEngine sslEngine = SampleOftpSslContextFactory.getClientContext().createSSLEngine();
		sslEngine.setUseClientMode(true);
		sslEngine.setEnableSessionCreation(true);

		TcpClient oftp = new TcpClient(new InetSocketAddress(host, port), sslEngine, factory);

		oftp.connect(true);

		compressedFile.delete();
	}

}
