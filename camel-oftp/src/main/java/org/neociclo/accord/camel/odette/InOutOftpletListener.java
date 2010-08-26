package org.neociclo.accord.camel.odette;

import static org.neociclo.odetteftp.protocol.AnswerReason.DUPLICATE_FILE;
import static org.neociclo.odetteftp.util.OdetteFtpSupport.getReplyDeliveryNotification;

import java.io.File;

import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.DefaultStartFileResponse;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.support.InOutOftpletEventListenerAdapter;

public class InOutOftpletListener extends InOutOftpletEventListenerAdapter {

	private OdetteEndpoint endpoint;
	private FileRenameBean fileRename;

	public InOutOftpletListener(OdetteEndpoint endpoint) {
		this.endpoint = endpoint;
		this.fileRename = endpoint.getConfiguration().getFileRenameBean();
	}

	@Override
	public StartFileResponse acceptStartFile(VirtualFile incomingFile) {
		File tmpDir = endpoint.getConfiguration().getTmpDir();
		File saveToFile = new File(tmpDir, fileRename.renameFile(incomingFile));

		// handle duplicate file
		if (saveToFile.exists()) {
			DefaultStartFileResponse duplicateFile = new DefaultStartFileResponse(false, DUPLICATE_FILE,
					"File already exist in local system.", true);
			return duplicateFile;
		}

		DefaultStartFileResponse acceptedFile = new DefaultStartFileResponse(true);
		acceptedFile.setFile(saveToFile);

		return acceptedFile;
	}

	@Override
	public void onReceiveFileStart(VirtualFile virtualFile, long answerCount) {
		System.out.println("Begin receiving file: " + virtualFile);
	}

	@Override
	public boolean onReceiveFileEnd(VirtualFile virtualFile, long recordCount, long unitCount) {
		// reply with EERP (positive delivery notification)
		DeliveryNotification notif = getReplyDeliveryNotification(virtualFile);
		endpoint.getOdetteOperations().offer(notif);

		// send the EERP back - request change direction (true)
		return true;
	}

}
