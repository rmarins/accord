package org.neociclo.accord.camel.odette;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.InOutSharedQueueOftpletFactory;
import org.neociclo.odetteftp.support.SessionConfig;

public class OdetteOperations {

	private OdetteEndpoint endpoint;
	private TcpClient client;
	private Queue<OdetteFtpObject> outgoingQueue = new ConcurrentLinkedQueue<OdetteFtpObject>();
	private Queue<OdetteFtpObject> incomingQueue = new ConcurrentLinkedQueue<OdetteFtpObject>();

	public OdetteOperations(OdetteEndpoint odetteEndpoint) {
		this.endpoint = odetteEndpoint;

		final OdetteConfiguration cfg = endpoint.getConfiguration();

		SessionConfig session = new SessionConfig();
		session.setUserCode(cfg.getOid());
		session.setUserPassword(cfg.getPassword());
		session.setTransferMode(cfg.getTransferMode());
		session.setDataExchangeBufferSize(cfg.getBufferSize());
		session.setWindowSize(cfg.getWindowSize());

		InOutSharedQueueOftpletFactory factory = new InOutSharedQueueOftpletFactory(session, outgoingQueue, null,
				incomingQueue);

		// prepare the incoming handler
		factory.setEventListener(new InOutOftpletListener(endpoint));

		client = new TcpClient(cfg.getHost(), cfg.getPort(), factory);
	}

	public boolean isConnected() {
		return client.isConnected();
	}

	/**
	 * <p>
	 * Starts polling files and notifications from Odette server. If Producer
	 * has added any file/notification to be sent, will do during this session.
	 * </p>
	 * 
	 * @throws Exception
	 * 
	 * @throws ClientException
	 */
	public void pollServer() throws Exception {
		client.connect(true);
	}

	public void offer(DeliveryNotification notif) {
		outgoingQueue.offer(notif);
	}

}
