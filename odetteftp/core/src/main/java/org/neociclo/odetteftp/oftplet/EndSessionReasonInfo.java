/**
 * Neociclo Accord, Open Source B2Bi Middleware
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
 *
 * $Id$
 */
package org.neociclo.odetteftp.oftplet;

import org.neociclo.odetteftp.protocol.EndSessionReason;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class EndSessionReasonInfo {

    private EndSessionReason endSessionReason;
    private String reasonText;

    public EndSessionReasonInfo(EndSessionReason reason) {
        this(reason, null);
    }

    public EndSessionReasonInfo(EndSessionReason endSessionReason, String reasonText) {
        super();
        if (endSessionReason == null) {
            throw new NullPointerException("endSessionReason");
        }
        this.endSessionReason = endSessionReason;
        this.reasonText = reasonText;
    }

    public EndSessionReason getEndSessionReason() {
        return endSessionReason;
    }

    public String getReasonText() {
        return reasonText;
    }

}
