package org.neociclo.accord.components.oftpcmd;

import static org.neociclo.accord.filetransfer.util.SecurityContextHelper.createUserPasswordCallbackContext;

import org.neociclo.accord.core.ContainerFactory;
import org.neociclo.accord.core.IContainer;
import org.neociclo.accord.filetransfer.IConnectContext;
import org.neociclo.accord.filetransfer.IOutgoingFileTransferContainerAdapter;
import org.neociclo.accord.filetransfer.ITransientConnectionContainerAdapter;

public class OftpProcess {

	private static final String OFTP_PROVIDER = "accord.odette-ftp";

	private OftpParameters options;

	public OftpProcess(OftpParameters options) {
		this.options = options;
	}

	public void start() {
		// create container
		IContainer oftpc = ContainerFactory.getDefault().createContainer(
				OFTP_PROVIDER);

		// get outgoing file transfer container adapter
		if (options.getFile() != null) {
			IOutgoingFileTransferContainerAdapter oftpSender = oftpc
					.getAdapter(IOutgoingFileTransferContainerAdapter.class);

		}

		// do transfer
		ITransientConnectionContainerAdapter oftpConn = oftpc
				.getAdapter(ITransientConnectionContainerAdapter.class);
		String targetID = "oftps://O0055PARTNERA@localhost:6619";
		IConnectContext targetConnectContext = createUserPasswordCallbackContext(
				"O0055PARTNERA", "NEOCICLO");
		oftpConn.execute(targetID, targetConnectContext);

	}

}
