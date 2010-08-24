package org.neociclo.accord.camel.odette;

import org.neociclo.odetteftp.protocol.v20.DefaultEnvelopedVirtualFile;

public class OdetteEnvelopedFileMessage extends OdetteMessage<DefaultEnvelopedVirtualFile> {

	private DefaultEnvelopedVirtualFile file;

	public OdetteEnvelopedFileMessage(DefaultEnvelopedVirtualFile file) {
		this.file = file;
	}

	@Override
	public DefaultEnvelopedVirtualFile getOdetteObject() {
		return file;
	}

}
