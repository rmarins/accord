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

import org.neociclo.odetteftp.oftplet.EndFileResponse;

public class DefaultEndFileResponse implements EndFileResponse {

    private static final long serialVersionUID = 8275980742439986266L;

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
