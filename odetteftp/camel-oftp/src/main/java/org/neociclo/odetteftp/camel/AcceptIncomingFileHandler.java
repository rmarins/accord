/**
 * Neociclo Accord, Open Source B2B Integration Suite

 * Copyright (C) 2005-2010 Neociclo, http://www.neociclo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neociclo.odetteftp.camel;

import static org.neociclo.odetteftp.protocol.DefaultStartFileResponse.*;

import java.io.File;
import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.apache.camel.spi.Synchronization;
import org.apache.commons.io.FileSystemUtils;
import org.neociclo.odetteftp.protocol.DefaultStartFileResponse;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.AnswerReason;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcceptIncomingFileHandler implements Synchronization {

	private static final Logger LOGGER = LoggerFactory.getLogger(AcceptIncomingFileHandler.class);

	private VirtualFile incomingFile;
	private AnswerReason userReason = AnswerReason.UNSPECIFIED;
	private File userFile;
	private boolean accepted = true;
	private String text;
	private boolean retryLater = true;
	/** reinit transfer from block 0 */
	private boolean forceBegin;
	private long restartOffset;

	private final boolean overwrite;
	private final FileRenameBean fileRenameBean;
	private final File workpath;
	private final long maxFileSize;
	private final boolean isRestart;

	public AcceptIncomingFileHandler(VirtualFile incomingFile, OftpSettings settings) {
		this.incomingFile = incomingFile;
		this.overwrite = settings.isOverwrite();
		this.maxFileSize = settings.getMaxFileSize();
		this.fileRenameBean = settings.getFileRenameBean();
		this.workpath = settings.getWorkpath();
		this.isRestart = settings.isRestart();
	}

	public VirtualFile getVirtualFile() {
		return incomingFile;
	}

	public void setNegativeAnswer(AnswerReason reason, String text, boolean retryLater) {
		this.userReason = reason;
		this.text = text;
		this.retryLater = retryLater;
	}

	public void setFile(File userFile) {
		this.userFile = userFile;
	}

	public void acceptFile() {
		this.accepted = true;
	}

	public void rejectFile() {
		this.accepted = false;
	}

	public void forceBegin() {
		this.forceBegin = true;
	}

	public void retryLater() {
		this.retryLater = true;
	}

	private File defineLocalFile() {
		File localFile = userFile;
		if (localFile == null) {
			String filename = fileRenameBean.renameFile(incomingFile);
			localFile = new File(workpath, filename);
		}
		return localFile;
	}

	public DefaultStartFileResponse createStartFileResponse() {
		File localFile = defineLocalFile();

		//
		// Check receive file violations
		//

		if (localFile.exists() && !overwrite) {
			return negativeStartFileAnswer(AnswerReason.DUPLICATE_FILE, text, retryLater);
		}

		if ((maxFileSize > 0 && incomingFile.getSize() > maxFileSize)) {
			return negativeStartFileAnswer(AnswerReason.FILE_SIZE_EXCEED, text, false);
		}

		if (!checkFileSystemSpace(localFile, incomingFile.getSize())) {
			return negativeStartFileAnswer(AnswerReason.FILE_SIZE_EXCEED,
					"No enough space available to save incoming file", false);
		}

		//
		// Give the answer according with replied exchange
		//

		if (accepted) {
			if (!isRestart && forceBegin) {
				restartOffset = 0L;
			}
			return positiveStartFileAnswer(localFile, restartOffset);
		} else {
			return negativeStartFileAnswer(userReason, text, retryLater);
		}
	}

	private boolean checkFileSystemSpace(File localFile, long fileSize) {
		try {
			long freeSpace = FileSystemUtils.freeSpaceKb(localFile.getParent());
			return freeSpace > fileSize;
		} catch (IOException e) {
			LOGGER.warn("Tried to check file system's free space", e);
		}

		return true;
	}

	public void onComplete(Exchange exchange) {

		Message out = exchange.getOut();

		StartFileResponse response;
		try {
			response = out.getMandatoryBody(StartFileResponse.class);
		} catch (InvalidPayloadException ipe) {
			setNegativeAnswer(AnswerReason.ACCESS_METHOD_FAILURE,
					"Cannot accept file: invalid payload exchange received.", false);
			exchange.setException(ipe);

			Object body = out.getBody();
			LOGGER.warn("Invalid payload in response message. A StartFileResponse type were expected. Was: "
					+ (body == null ? "null" : body.getClass().getName()), ipe);

			return;
		}

		if (response.accepted()) {
			acceptFile();
			restartOffset = response.getRestartOffset();

			File saveTo = response.getFile();
			if (saveTo != null) {
				setFile(saveTo);
			}
		} else {
			rejectFile();
			setNegativeAnswer(response.getReason(), response.getReasonText(), response.retryLater());
		}

	}

	public void onFailure(Exchange exchange) {
		rejectFile();

		Message out = exchange.getOut();
		StartFileResponse response = out.getBody(StartFileResponse.class);

		if (response != null) {
			setNegativeAnswer(response.getReason(), response.getReasonText(), response.retryLater());
		}

	}
}
