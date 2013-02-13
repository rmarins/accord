/**
 * Neociclo Accord, Open Source B2Bi Middleware
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
package org.neociclo.odetteftp.protocol;

import java.io.File;

import org.neociclo.odetteftp.oftplet.StartFileResponse;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
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
