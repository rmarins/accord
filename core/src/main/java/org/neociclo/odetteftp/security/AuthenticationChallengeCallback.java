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
package org.neociclo.odetteftp.security;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

/**
 * Convey the received Authentication Challenge request/response with the
 * {@link CallbackHandler}.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class AuthenticationChallengeCallback implements Callback {

    private byte[] encodedChallenge;
    private byte[] challenge;
    
    /**
     * Constructor.
     * 
     * @param challenge received encoded challenge
     */
    public AuthenticationChallengeCallback(byte[] challenge) {
        super();
        this.encodedChallenge = challenge;
    }

    public byte[] getEncodedChallenge() {
        return encodedChallenge;
    }

    public byte[] getChallenge() {
        return challenge;
    }

    public void setChallenge(byte[] plainChallenge) {
        this.challenge = plainChallenge;
    }

}
