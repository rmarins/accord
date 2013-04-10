/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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
package org.neociclo.odetteftp;

import static org.junit.Assert.*;
import static org.neociclo.odetteftp.OdetteFtpVersion.*;

import org.junit.Test;
import org.neociclo.odetteftp.ProtocolHandlerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class ProtocolStrategyFactoryTest {

    @Test
    public void testSupportedProtocolVersions() {
        assertTrue(OFTP_V12.toString(), ProtocolHandlerFactory.isProtocolVersionSupported(OFTP_V12));
        assertTrue(OFTP_V13.toString(), ProtocolHandlerFactory.isProtocolVersionSupported(OFTP_V13));
        assertTrue(OFTP_V14.toString(), ProtocolHandlerFactory.isProtocolVersionSupported(OFTP_V14));
        assertTrue(OFTP_V20.toString(), ProtocolHandlerFactory.isProtocolVersionSupported(OFTP_V20));
    }

}
