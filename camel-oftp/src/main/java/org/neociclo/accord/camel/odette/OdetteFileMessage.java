package org.neociclo.accord.camel.odette;

import org.neociclo.odetteftp.protocol.DefaultVirtualFile;

public class OdetteFileMessage extends OdetteMessage<DefaultVirtualFile> {

	private DefaultVirtualFile file;

	public OdetteFileMessage(DefaultVirtualFile file) {
		this.file = file;
	}

	@Override
	public DefaultVirtualFile getOdetteObject() {
		return file;
	}

}
