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
package org.neociclo.odetteftp.service;

import static org.junit.Assert.*;

import org.junit.Test;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class IsdnExternalConnectionTest extends AbstractIsdnClientExternal {

    @Override
    protected OdetteFtpConfiguration createSessionConfig() {
    	OdetteFtpConfiguration c = new OdetteFtpConfiguration();
    	c.setVersion(OdetteFtpVersion.OFTP_V12);
        c.setTransferMode(TransferMode.SENDER_ONLY);
//        c.setHasSpecialLogic(true);
        return c;
    }

    @Test
    public void testConnectAndDisconnect() throws Exception {

        if (!runTests) {
            return;
        }

        connect(false);
        assertTrue(client.isConnected());

        client.awaitDisconnect();
        assertFalse(client.isConnected());

    }

}
