/**
 * Neociclo Accord, Open Source B2B Integration Suite

 * Copyright (C) 2005-2010 Neociclo, http://www.neociclo.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id$
 */
package org.neociclo.accord.odetteftp.camel;

import java.io.File;
import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.spi.Synchronization;
import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neociclo.odetteftp.protocol.AnswerReason;
import org.neociclo.odetteftp.protocol.DefaultStartFileResponse;
import org.neociclo.odetteftp.protocol.VirtualFile;

class IncomingFileResponse implements Synchronization {

	private static final Log log = LogFactory.getLog(IncomingFileResponse.class);

	private VirtualFile incomingFile;
	private AnswerReason userReason = AnswerReason.UNSPECIFIED;
	private OdetteConfiguration config;
	private File userFile;
	private boolean accepted = true;
	private String text;
	private boolean retryLater = true;
	private boolean override;
	private boolean forceRestart;

	public IncomingFileResponse(VirtualFile incomingFile, OdetteConfiguration config) {
		this.incomingFile = incomingFile;
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

	public void setFile(File userFile) {
		this.userFile = userFile;
	}

	public void acceptFile() {
		this.accepted = true;
	}

	public void rejectFile() {
		this.accepted = false;
	}

	public void forceRestart() {
		this.forceRestart = true;
	}

	public void retryLater() {
		this.retryLater = true;
	}

	private File defineLocalFile() {
		FileRenameBean fileRenameBean = config.getFileRenameBean();
		String filename = fileRenameBean.renameFile(incomingFile);
		File localFile = userFile != null ? userFile : new File(config.getWorkpath(), filename);
		return localFile;
	}

	public DefaultStartFileResponse createStartFileResponse() {
		File localFile = defineLocalFile();

		if (localFile.exists() && !override) {
			return DefaultStartFileResponse.negativeStartFileAnswer(AnswerReason.DUPLICATE_FILE, text, retryLater);
		}

		long maxFileSize = config.getMaxFileSize();
		if ((maxFileSize > 0 && incomingFile.getSize() > maxFileSize)) {
			return DefaultStartFileResponse.negativeStartFileAnswer(AnswerReason.FILE_SIZE_EXCEED, text, false);
		}

		if (!checkFileSystemSpace(localFile, incomingFile.getSize())) {
			return DefaultStartFileResponse.negativeStartFileAnswer(AnswerReason.FILE_SIZE_EXCEED,
					"No enough space available to save incoming file", false);
		}

		if (accepted) {
			long restartOffset = 0;
			if (config.getAutoResume() && localFile.exists() && !forceRestart) {
				restartOffset = localFile.length();
			}
			return DefaultStartFileResponse.positiveStartFileAnswer(localFile, restartOffset);
		} else {
			return DefaultStartFileResponse.negativeStartFileAnswer(userReason, text, retryLater);
		}
	}

	private boolean checkFileSystemSpace(File localFile, long fileSize) {
		fileSize = fileSize / 1024; // convert to Kb
		try {
			long freeSpace = FileSystemUtils.freeSpaceKb(localFile.getParent());
			return freeSpace > fileSize;
		} catch (IOException e) {
			log.warn("Tried to check file system's free space", e);
		}

		return true;
	}

	public void onComplete(Exchange exchange) {
		acceptFile();

		Message out = exchange.getOut();
		File saveTo = out.getBody(File.class);
		if (saveTo != null) {
			setFile(saveTo);
		}

		Boolean headerOverride = out.getHeader(OdetteEndpoint.ODETTE_OVERRIDE, Boolean.class);
		if (headerOverride != null) {
			setOverride(headerOverride.booleanValue());
		}

		Boolean headerForceRestart = out.getHeader(OdetteEndpoint.ODETTE_FORCE_RESTART, Boolean.class);
		if (headerForceRestart != null && headerForceRestart.booleanValue()) {
			forceRestart();
		}
	}

	public void onFailure(Exchange exchange) {
		rejectFile();

		Message out = exchange.getOut();
		AnswerReason reason = out.getHeader(OdetteEndpoint.ODETTE_ANSWER_REASON, AnswerReason.UNSPECIFIED,
				AnswerReason.class);
		String reasonText = out.getHeader(OdetteEndpoint.ODETTE_REASON_TEXT, String.class);
		setNegativeAnswer(reason, reasonText);

		Boolean headerRetryLater = out.getHeader(OdetteEndpoint.ODETTE_RETRY_LATER, Boolean.class);
		if (headerRetryLater != null && headerRetryLater.booleanValue()) {
			retryLater();
		}
	}
}
