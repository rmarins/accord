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
package org.neociclo.odetteftp.camel.test;

import static org.neociclo.odetteftp.TransferMode.SENDER_ONLY;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.examples.support.DefaultOftpletFactory;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.InOutSharedQueueOftpletFactory;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.support.OftpletEventListener;
import org.neociclo.odetteftp.support.PasswordHandler;

/**
 * @author Rafael Marins
 */
public abstract class BaseClientTestSupport extends OftpTestSupport {

    static {
        String path = System.getProperty("java.security.auth.login.config");
        if (path == null) {
            URL resource = BaseClientTestSupport.class.getClassLoader().getResource("server-login.config");
            if (resource != null) {
                path = resource.getFile();
                System.setProperty("java.security.auth.login.config", path);
            }
        }
    }

	protected String clientDirName = "client-data";

	// Member methods
	// -------------------------------------------------------------------------

	@Override
	protected void setUp() throws Exception {
		initPort();
		createDirectory(getOutputDir().getAbsolutePath());
		createDirectory(getTempDir().getAbsolutePath());
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		resetPort();
		deleteDirectory(getTempDir());
		deleteDirectory(getOutputDir());
		super.tearDown();
	}

	public String getClientDirName() {
		return clientDirName;
	}

	@Override
	protected File getOutputDir() {
		return new File(super.getOutputDir(), getClientDirName());
	}

	protected File getTempDir() {
		return new File(getOutputDir(), "tmp");
	}

	// Odette FTP client template methods
	// -------------------------------------------------------------------------

	protected void sendFile(File payload, String oid, String pwd, String dest) throws Exception {

		OdetteFtpConfiguration conf = new OdetteFtpConfiguration();
		conf.setTransferMode(SENDER_ONLY);

		MappedCallbackHandler securityCallbacks = new MappedCallbackHandler();
		securityCallbacks.addHandler(PasswordCallback.class,
				new PasswordHandler(oid, pwd));

		Queue<OdetteFtpObject> filesToSend = new ConcurrentLinkedQueue<OdetteFtpObject>();

		DefaultVirtualFile vf = new DefaultVirtualFile();
		vf.setDatasetName(payload.getName());
		vf.setDestination(dest);
		vf.setFile(payload);

		filesToSend.offer(vf);

		OftpletFactory factory = new InOutSharedQueueOftpletFactory(conf, securityCallbacks, filesToSend, null, null);
		TcpClient client = new TcpClient("localhost", getPort(), factory);

		client.disableLogging();
		client.connect(true);
		
	}

	protected void clientConnectAndDisconnect(String usercode, String pwd) throws Exception {

		String server = "localhost";

		MappedCallbackHandler securityCallbacks = new MappedCallbackHandler();
		securityCallbacks.addHandler(PasswordCallback.class, new PasswordHandler(usercode, pwd));

		OftpletFactory factory = new DefaultOftpletFactory(securityCallbacks);
		TcpClient client = new TcpClient(server, getPort(), factory);

		client.disableLogging();
		client.connect(true);
		
	}

	protected List<OdetteFtpObject> clientReceiveAllFiles(String usercode, String pwd, OftpletEventListener eventListener) throws Exception {

		String server = "localhost";

		OdetteFtpConfiguration conf = getInitialClientConfig();
		conf.setTransferMode(TransferMode.BOTH);

		MappedCallbackHandler securityCallbacks = new MappedCallbackHandler();
		securityCallbacks.addHandler(PasswordCallback.class, new PasswordHandler(usercode, pwd));

		final Queue<OdetteFtpObject> incomingQueue = new ConcurrentLinkedQueue<OdetteFtpObject>();
		final Queue<OdetteFtpObject> outgoingQueue = new ConcurrentLinkedQueue<OdetteFtpObject>();

		InOutSharedQueueOftpletFactory factory = new InOutSharedQueueOftpletFactory(conf, securityCallbacks,
				outgoingQueue, null, incomingQueue);

		factory.setEventListener(eventListener);

		TcpClient client = new TcpClient(server, getPort(), factory);

		client.disableLogging();
		client.connect(true);

		ArrayList<OdetteFtpObject> result = new ArrayList<OdetteFtpObject>();
		result.addAll(incomingQueue);

		return result;
	}

	protected OdetteFtpConfiguration getInitialClientConfig() {
		OdetteFtpConfiguration c = new OdetteFtpConfiguration();
		return c;
	}

	protected File createTempFile(String body) throws IOException {
		File payloadFile = File.createTempFile("test-", null, getTempDir());
		FileOutputStream outStream = new FileOutputStream(payloadFile);
		OutputStreamWriter w = new OutputStreamWriter(outStream);
		w.write(body);
		w.flush();
		outStream.flush();
		outStream.close();
		return payloadFile;
	}

}
