package org.neociclo.accord.camel.odette;

import java.io.File;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import org.neociclo.odetteftp.security.PasswordCallback;

public class OdetteOperations {

	private OdetteEndpoint endpoint;
	// TODO bind audit listener
	private OdetteAuditListener auditListener;
	private TransientClient client;

	public OdetteOperations(OdetteEndpoint odetteEndpoint) {
		this.endpoint = odetteEndpoint;

		final OdetteConfiguration cfg = endpoint.getConfiguration();
		this.auditListener = cfg.getListener();

		ClientParameters oftpParams = new ClientParameters();
		oftpParams.setHost(cfg.getHost());
		oftpParams.setMode(cfg.getTransferMode());
		oftpParams.setPort(cfg.getPort());

		ISecurityContext context = new ISecurityContext() {
			public CallbackHandler getCallbackHandler() {
				return new CallbackHandler() {
					public void handle(Callback[] callbacks)
							throws CallbackException {
						for (Callback cb : callbacks) {
							if (cb instanceof PasswordCallback) {
								((PasswordCallback) cb).setPassword(cfg
										.getPassword());
							}
						}
					}
				};
			}
		};

		File tmpDir = cfg.getTmpDir();

		this.client = new TransientClient(oftpParams, context, tmpDir);
	}

	public boolean isWorking() {
		return client.isPerformingTransfer();
	}

	public void sendDeliveryNotification(DeliveryNotificationInfo oftpDelivery) {
		client.addSendNotification(oftpDelivery);
	}

	public void sendOftpFile(OftpFile oftpFile) {
		client.addSendFile(oftpFile);
	}

	/**
	 * <p>
	 * Starts polling files and notifications from Odette server. If Producer
	 * has added any file/notification to be sent, will do during this session.
	 * </p>
	 * 
	 * @throws ClientException
	 */
	public void startSession() throws ClientException {
		client.transfer();
	}

	public void addIncomingTaskListener(
			final OdetteIncomingTaskListener listener) {
		client.addListener(new IOdetteFtpListener() {

			public void handleOdetteFtpEvent(OdetteFtpEvent event) {
				if (event instanceof DeliveryNotificationEvent) {
					listener.incoming(((DeliveryNotificationEvent) event)
							.getNotification());
				}

				// TODO identify this is a transfer event for incoming file
				if (event instanceof FileTransferEndEvent) {
					FileTransferEndEvent fileTransferEvent = (FileTransferEndEvent) event;

					File file = fileTransferEvent.getVirtualFile().getFile();
					VirtualFileInfo info = fileTransferEvent.getMappingInfo();

					OftpFile ofile = new OftpFile(file, info);
					listener.incoming(ofile);
				}
			}
		});
	}
}
