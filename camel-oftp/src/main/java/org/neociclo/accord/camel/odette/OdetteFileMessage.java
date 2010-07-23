package org.neociclo.accord.camel.odette;

import org.neociclo.odetteftp.client.OftpFile;

public class OdetteFileMessage extends OdetteMessage<OftpFile> {

	private OftpFile file;

	public OdetteFileMessage(OftpFile file) {
		this.file = file;
	}

	@Override
	public OftpFile getOdetteObject() {
		return file;
	}

}
