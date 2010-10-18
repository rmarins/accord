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

import org.neociclo.odetteftp.oftplet.EndFileResponse;

public class DefaultEndFileResponse implements EndFileResponse {

	public static EndFileResponse positiveEndFileAnswer() {
		return positiveEndFileAnswer(true);
	}

	public static DefaultEndFileResponse positiveEndFileAnswer(boolean changeDirection) {
		DefaultEndFileResponse response = new DefaultEndFileResponse(true, changeDirection);
		return response;
	}

	public static DefaultEndFileResponse negativeEndFileAnswer() {
		return negativeEndFileAnswer(AnswerReason.UNSPECIFIED, null);
	}

	public static DefaultEndFileResponse negativeEndFileAnswer(AnswerReason reason, String reasonText) {
		return new DefaultEndFileResponse(false, reason, reasonText);
	}

	private boolean accepted;
	private boolean changeDirection;
	private AnswerReason reason;
	private String reasonText;

	private DefaultEndFileResponse(boolean accepted, boolean changeDirection) {
		this.accepted = accepted;
		this.changeDirection = changeDirection;
	}

	private DefaultEndFileResponse(boolean accepted, AnswerReason reason, String reasonText) {
		this.accepted = accepted;
		this.reason = reason != null ? reason : AnswerReason.UNSPECIFIED;
		this.reasonText = reasonText;
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

	public boolean changeDirection() {
		return changeDirection;
	}

}
