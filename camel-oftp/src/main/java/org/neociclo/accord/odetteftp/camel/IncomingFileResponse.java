package org.neociclo.accord.odetteftp.camel;

import java.io.File;

import org.neociclo.odetteftp.protocol.AnswerReason;
import org.neociclo.odetteftp.protocol.DefaultStartFileResponse;
import org.neociclo.odetteftp.protocol.VirtualFile;

public class IncomingFileResponse {

	private FileRenameBean renameBean;
	private VirtualFile incomingFile;
	private AnswerReason userReason = AnswerReason.UNSPECIFIED;
	private OdetteConfiguration config;
	private String userFilename;
	private boolean accepted = true;
	private String text;
	private boolean retryLater = true;
	private boolean override;
	private boolean forceRestart;

	public IncomingFileResponse(VirtualFile incomingFile, FileRenameBean fileRenameBean, OdetteConfiguration config) {
		this.incomingFile = incomingFile;
		this.renameBean = fileRenameBean;
		this.config = config;
		this.override = config.getOverride();
	}

	public VirtualFile getVirtualFile() {
		return incomingFile;
	}

	public void setOverride(boolean override) {
		this.override = override;
	}

	public void setNegativeAnswer(AnswerReason reason, String text) {
		this.userReason = reason;
		this.text = text;
	}

	public void setFilename(String userFilename) {
		this.userFilename = userFilename;
	}

	public void acceptFile() {
		this.accepted = true;
	}

	public void rejectFile() {
		this.accepted = false;
	}

	public void forcedRestart() {
		this.forceRestart = true;
	}

	public void retryLater() {
		this.retryLater = true;
	}

	private File defineLocalFile() {
		String filename = userFilename != null ? userFilename : renameBean.renameFile(incomingFile);
		File localFile = new File(config.getTmpDir(), filename);
		return localFile;
	}

	public DefaultStartFileResponse createStartFileResponse() {
		File localFile = defineLocalFile();

		if (localFile.exists() && !override) {
			return DefaultStartFileResponse.negativeAnswer(AnswerReason.DUPLICATE_FILE, text, retryLater);
		}

		if (!checkFileSystemSpace()) {
			return DefaultStartFileResponse.negativeAnswer(AnswerReason.FILE_SIZE_EXCEED,
					"No enough space available to save incoming file", false);
		}

		long maxFileSize = config.getMaxFileSize();
		if ((maxFileSize > 0 && incomingFile.getSize() > maxFileSize)) {
			return DefaultStartFileResponse.negativeAnswer(AnswerReason.FILE_SIZE_EXCEED, text, false);
		}

		if (accepted) {
			long restartOffset = 0;
			if (config.getAutoResume() && localFile.exists() && !forceRestart) {
				restartOffset = localFile.length();
			}
			return DefaultStartFileResponse.positiveAnswer(localFile, restartOffset);
		} else {
			return DefaultStartFileResponse.negativeAnswer(userReason, text, retryLater);
		}
	}

	private boolean checkFileSystemSpace() {
		// TODO check if there's enough space to save
		return true;
	}
}
