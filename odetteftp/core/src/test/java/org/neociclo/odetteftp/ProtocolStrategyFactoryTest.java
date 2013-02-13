/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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
