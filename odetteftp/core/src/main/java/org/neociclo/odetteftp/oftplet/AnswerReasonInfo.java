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
package org.neociclo.odetteftp.oftplet;

import org.neociclo.odetteftp.protocol.AnswerReason;

/**
 * @author Rafael Marins
 */
public class AnswerReasonInfo {

    private AnswerReason answerReason;
    private String reasonText;

    public AnswerReasonInfo(AnswerReason answerReason) {
        this(answerReason, null);
    }

    public AnswerReasonInfo(AnswerReason answerReason, String reasonText) {
        super();
        if (answerReason == null) {
            throw new NullPointerException("answerReason");
        }
        this.answerReason = answerReason;
        this.reasonText = reasonText;
    }

    public AnswerReason getAnswerReason() {
        return answerReason;
    }

    public String getReasonText() {
        return reasonText;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName());
        sb.append("(").append(answerReason);
        if (reasonText != null) {
            sb.append(", ").append(reasonText);
        }
        sb.append(")");
        return sb.toString();
    }
}
