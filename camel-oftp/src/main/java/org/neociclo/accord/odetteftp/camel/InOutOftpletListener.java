package org.neociclo.accord.odetteftp.camel;

import static org.neociclo.odetteftp.util.OdetteFtpSupport.getReplyDeliveryNotification;

import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.support.OftpletEventListenerAdapter;

public class InOutOftpletListener extends OftpletEventListenerAdapter {

	private OdetteEndpoint endpoint;

	public InOutOftpletListener(OdetteEndpoint endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public StartFileResponse acceptStartFile(VirtualFile incomingFile) {
		OdetteHandler userHandler = endpoint.getConfiguration().getHandler();

		FileRenameBean fileRenameBean = endpoint.getConfiguration().getFileRenameBean();
		IncomingFileResponse incomingFileResponse = new IncomingFileResponse(incomingFile, fileRenameBean,
				endpoint.getConfiguration());

		userHandler.acceptIncoming(incomingFileResponse);

		return incomingFileResponse.createStartFileResponse();
	}

	@Override
	public void onReceiveFileStart(VirtualFile virtualFile, long answerCount) {
	}

	@Override
	public boolean onReceiveFileEnd(VirtualFile virtualFile, long recordCount, long unitCount) {
		endpoint.notifyConsumersOf(virtualFile);

		// reply with EERP (positive delivery notification) if
		// 'alwaysReplyDelivery' is true
		if (endpoint.getConfiguration().isAlwaysReplyDelivery()) {
			DeliveryNotification notif = getReplyDeliveryNotification(virtualFile);
			endpoint.getOdetteOperations().offer(notif);
		}

		// send the EERP back - request change direction (true)
		return true;
	}

	@Override
	public void onSendFileEnd(VirtualFile virtualFile) {
		endpoint.getOdetteOperations().virtualFileSent(virtualFile);
	}
	
	@Override
	public void onNotificationReceived(DeliveryNotification notif) {
		endpoint.notifyConsumersOf(notif);
	}
	
}
