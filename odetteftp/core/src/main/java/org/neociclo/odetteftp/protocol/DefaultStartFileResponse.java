/**
 * The Accord Project, http://accordproject.org
 * Copyright (C) 2005-2013 Rafael Marins, http://rafaelmarins.com
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
package org.neociclo.odetteftp.protocol;

import java.io.File;

import org.neociclo.odetteftp.oftplet.StartFileResponse;

/**
 * @author Rafael Marins
 */
public class DefaultStartFileResponse implements StartFileResponse {

	private static final long serialVersionUID = 1L;

	private boolean accepted;
	private long restartOffset;

	private AnswerReason reason;
	private String reasonText;
	private boolean retryLater;

	private File file;

	public static DefaultStartFileResponse positiveStartFileAnswer(File saveTo) {
		return positiveStartFileAnswer(saveTo, 0);
	}

	public static DefaultStartFileResponse positiveStartFileAnswer(File saveTo, long restartOffset) {
		if (saveTo == null) {
			throw new NullPointerException("saveTo");
		}
		DefaultStartFileResponse response = new DefaultStartFileResponse(true, restartOffset);
		response.file = saveTo;
		return response;
	}

	public static DefaultStartFileResponse negativeStartFileAnswer() {
		return negativeStartFileAnswer(AnswerReason.UNSPECIFIED, null, true);
	}

	public static DefaultStartFileResponse negativeStartFileAnswer(AnswerReason reason, String reasonText, boolean retryLater) {
		return new DefaultStartFileResponse(false, reason, reasonText, retryLater);
	}

	protected DefaultStartFileResponse(boolean accepted, long restartOffset) {
		super();
		this.accepted = accepted;
		this.restartOffset = restartOffset;
	}

	protected DefaultStartFileResponse(boolean accepted, AnswerReason reason, String reasonText, boolean retryLater) {
		super();
		this.accepted = accepted;
		this.reason = reason;
		this.reasonText = reasonText;
		this.retryLater = retryLater;
	}

	public boolean accepted() {
		return accepted;
	}

	public AnswerReason getReason() {
		return reason;
	}

	public String getReasonText() {
		return reasonText;
	}

	public long getRestartOffset() {
		return restartOffset;
	}

	public boolean retryLater() {
		return retryLater;
	}

	public File getFile() {
		return file;
	}

}
