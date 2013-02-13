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
package org.neociclo.odetteftp.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class ExternalConnectionTest extends AbstractTcpClientExternal {

    @Override
    protected OdetteFtpConfiguration createSessionConfig() {
    	OdetteFtpConfiguration c = new OdetteFtpConfiguration();
        c.setTransferMode(TransferMode.SENDER_ONLY);
        return c;
    }

    @Test
    public void testConnectAndDisconnect() throws Exception {

        if (!runTests) {
            return;
        }

        connect();
        assertTrue(client.isConnected());

        client.awaitDisconnect();
        assertFalse(client.isConnected());

    }

}
