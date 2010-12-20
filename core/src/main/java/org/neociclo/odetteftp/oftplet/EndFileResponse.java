/**
 *  Neociclo Accord - Open Source B2B Integration Suite
 *  Copyright (C) 2005-2008 Neociclo, http://www.neociclo.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package org.neociclo.odetteftp.oftplet;

import java.io.Serializable;

import org.neociclo.odetteftp.protocol.AnswerReason;

public interface EndFileResponse extends Serializable {

    /**
     * @return if this is a positive or negative end file esponse
     */
    boolean accepted();

    /**
     * @return reason for negative end file response
     */
    AnswerReason getReason();

    /**
     * @return description for negative end file response
     */
    String getReasonText();

    /**
     * @return if a change direction (CD) must be peformed
     */
    boolean changeDirection();

}
