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
package org.neociclo.odetteftp.camel;

import static org.neociclo.odetteftp.camel.OftpEndpointUtil.*;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;

import javax.net.ssl.SSLEngine;

import org.apache.camel.util.ObjectHelper;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.camel.jaas.JaasPasswordAuthenticator;
import org.neociclo.odetteftp.camel.ssl.DefaultSSLEngineFactory;
import org.neociclo.odetteftp.camel.ssl.SSLEngineFactory;
import org.neociclo.odetteftp.protocol.DefaultEndFileResponse;
import org.neociclo.odetteftp.oftplet.EndFileResponse;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.PasswordAuthenticationCallback;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.service.Client;
import org.neociclo.odetteftp.service.Server;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.service.TcpServer;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.support.PasswordHandler;
import org.neociclo.odetteftp.util.IoUtil;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpOperations {

	private IOftpEndpoint endpoint;

	public OftpOperations(IOftpEndpoint endpoint) {
		super();
		this.endpoint = endpoint;
	}

	public OftpSettings getSettings() {
		return getEndpoint().getSettings();
	}

	public StartFileResponse acceptStartFile(OdetteFtpSession session, VirtualFile vf) {
		AcceptIncomingFileHandler acceptor = askConsumerForIncomingFile(getEndpoint(), session, vf);
		return acceptor.createStartFileResponse();
	}

	public EndFileResponse onReceiveFileEnd(OdetteFtpSession session, VirtualFile virtualFile, long recordCount, long unitCount) {
		notifyConsumerOf(getEndpoint(), virtualFile);
		return DefaultEndFileResponse.positiveEndFileAnswer(false);
	}

	protected MappedCallbackHandler createServerSecurityHandler() {

		OftpSettings settings = getEndpoint().getSettings();
		MappedCallbackHandler securityCallbacks = new MappedCallbackHandler();

		// password callback - retrieve oid & pwd of this entity user
		securityCallbacks.addHandler(PasswordCallback.class,
				new PasswordHandler(settings.getUsercode(), settings.getPassword()));

		// authentication callback - validate connecting users using JAAS module
		securityCallbacks.addHandler(PasswordAuthenticationCallback.class, new JaasPasswordAuthenticator(
				settings.getJaasRealm()));

		return securityCallbacks;
	}

	protected MappedCallbackHandler createClientSecurityHandler() {

		OftpSettings settings = getEndpoint().getSettings();
		MappedCallbackHandler securityCallbacks = new MappedCallbackHandler();

		// password callback - retrieve oid & pwd of this entity user
		securityCallbacks.addHandler(PasswordCallback.class,
				new PasswordHandler(settings.getUsercode(), settings.getPassword()));

		return securityCallbacks;
	}

	public IOftpEndpoint getEndpoint() {
		return endpoint;
	}

	public void notifyEvent(OftpEvent event) {
		notifyConsumerOf(getEndpoint(), event);
	}

	public List<OdetteFtpObject> listUserOutgoingExchanges(String userCode, OdetteFtpSession session) {
		return askConsumerForUserOutgoingExchanges(getEndpoint(), userCode, session);
	}

	public void onSessionStart(OdetteFtpSession session) {
		notifyEvent(OftpEvent.onSessionStartEvent(session));
	}

	public void onSessionEnd(OdetteFtpSession session) {
		notifyEvent(OftpEvent.onSessionEndEvent(session));
	}

	// Client specific operations
	// -------------------------------------------------------------------------

	public Client initializeClient(boolean hasOut, boolean hasIn) throws Exception {
		OftpSettings settings = getSettings();

		// should it listen with secure connection enabled
		SSLEngine sslEngine = null;
		if (settings.isSsl()) {
			sslEngine = createClientSSLEngine();
		}

		OdetteFtpConfiguration config = settings.asOftpletConfiguration();
		if (hasOut && hasIn) {
			config.setTransferMode(TransferMode.BOTH);
		} else if (hasOut) {
			config.setTransferMode(TransferMode.SENDER_ONLY);
		} else if (hasIn) {
			config.setTransferMode(TransferMode.RECEIVER_ONLY);
		} else {
			return null;
		}

		// Using the framework support Oftplet factory working with Queues
		CamelOftpletFactory factory = new CamelOftpletFactory(config, createClientSecurityHandler(), this);

		InetSocketAddress address = new InetSocketAddress(settings.getHost(), settings.getPort());
		TcpClient client = new TcpClient(address, sslEngine, factory);

		// provide resources to setup the asynchronous networking framework
		client.setBossExecutor(getEndpoint().getBossExecutor());
		client.setWorkerExecutor(getEndpoint().getWorkerExecutor());
		client.setTimer(getEndpoint().getTimer());

		return client;
	}

	private SSLEngine createClientSSLEngine() throws Exception {
		SSLEngine sslEngine;
		OftpSettings settings = getSettings();

		if (settings.getSslEngine() != null) {
			sslEngine = settings.getSslEngine();
		} else {

			// validate mandatory endpoint parameters to enable SSL
			ObjectHelper.notNull(settings.getKeyStoreFormat(), "keyStoreFormat", endpoint);
			ObjectHelper.notNull(settings.getSecurityProvider(), "securityProvider", endpoint);
			ObjectHelper.notNull(settings.getTrustStoreFile(), "trustStoreFile", endpoint);

			// optionally use trustStore's passphrase
			char[] passphrase = null;
			if (settings.getPassphrase() != null) {
				passphrase = settings.getPassphrase().toCharArray();
			}

			SSLEngineFactory sslEngineFactory = new DefaultSSLEngineFactory();
			sslEngineFactory.setup(
					settings.getKeyStoreFormat(),
					settings.getSecurityProvider(),
					null,
					settings.getTrustStoreFile(),
					passphrase);

			sslEngine = sslEngineFactory.createClientSSLEngine();
		}
		return sslEngine;
	}

	public void runClient() throws Exception {

		final OftpClientEndpoint endpoint = (OftpClientEndpoint) getEndpoint();

		final boolean send = endpoint.hasOut();
		final boolean recv = endpoint.hasIn();

		Client client = endpoint.getOftpClient();
		if (client == null) {
			client = initializeClient(send, recv);
			endpoint.setOftpClient(client);
		}

		synchronized (client) {

			if (!send && !recv) {
				return;
			}

			if (client.isConnected()) {
				return;
			}

			Runnable onDisconnect = new Runnable() {
				public void run() {
					if (send) {
						endpoint.unsetHasOut();
					}
					if (recv) {
						endpoint.unsetHasIn();
					}
				}
			};

			client.setDisconnectListener(onDisconnect);
			client.connect(true);
		}

		endpoint.setOftpClient(null);
		
	}

	// Server specific operations
	// -------------------------------------------------------------------------


	public Server initializeServer() throws Exception {

		OftpSettings settings = getSettings();

		// should it listen with secure connection enabled
		SSLEngine sslEngine = null;
		if (settings.isSsl()) {
			sslEngine = createServerSSLEngine();
		}

		// Using the framework support Oftplet factory working with Queues
		CamelOftpletFactory factory = new CamelOftpletFactory(settings.asOftpletConfiguration(),
				createServerSecurityHandler(), this);

		InetSocketAddress address = new InetSocketAddress(settings.getHost(), settings.getPort());
		TcpServer server = new TcpServer(address, sslEngine, factory);

		// provide resources to setup the asynchronous networking framework
		server.setBossExecutor(getEndpoint().getBossExecutor());
		server.setWorkerExecutor(getEndpoint().getWorkerExecutor());
		server.setTimer(getEndpoint().getTimer());

		return server;
	}

	private SSLEngine createServerSSLEngine() throws Exception {

		SSLEngine sslEngine;
		OftpSettings settings = getSettings();

		if (settings.getSslEngine() != null) {
			sslEngine = settings.getSslEngine();
		} else {

			// validate mandatory endpoint parameters to enable SSL
			ObjectHelper.notNull(settings.getKeyStoreFormat(), "keyStoreFormat", endpoint);
			ObjectHelper.notNull(settings.getSecurityProvider(), "securityProvider", endpoint);
			ObjectHelper.notNull(settings.getKeyStoreFile(), "keyStoreFile", endpoint);
			ObjectHelper.notNull(settings.getTrustStoreFile(), "trustStoreFile", endpoint);
			ObjectHelper.notNull(settings.getPassphrase(), "passphrase", endpoint);

			SSLEngineFactory sslEngineFactory = new DefaultSSLEngineFactory();
			sslEngineFactory.setup(
					settings.getKeyStoreFormat(),
					settings.getSecurityProvider(),
					settings.getKeyStoreFile(),
					settings.getTrustStoreFile(),
					settings.getPassphrase().toCharArray());

			sslEngine = sslEngineFactory.createServerSSLEngine();
		}
		return sslEngine;
	}

	public boolean deleteFile(File file) {
		return IoUtil.delete(file);
	}

	public boolean existsFile(File file) {
		return IoUtil.existsFile(file);
	}

}
