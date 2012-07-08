/**
 * Neociclo Accord, Open Source B2B Integration Suite
 * Copyright (C) 2005-2012 Neociclo, http://www.neociclo.com
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
package org.neociclo.odetteftp.examples.proxy;

import static org.jboss.netty.buffer.ChannelBuffers.*;
import static org.jboss.netty.util.CharsetUtil.ISO_8859_1;
import static org.junit.Assert.*;
import static org.neociclo.odetteftp.examples.proxy.RemoteCapiProxy.*;

import org.jboss.netty.buffer.ChannelBuffer;
import org.junit.Test;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class RemoteCapiProxyTest {

	@Test
	public void test() {

		ChannelBuffer modified = copiedBuffer("X1O0013004468WABCOGRP000001WABCO   16384BYYN050             ", ISO_8859_1);
		ChannelBuffer expected = copiedBuffer("X1DINET                    WABCO   16384BYYN050             ", ISO_8859_1);
		bufferReplaceBy(modified, "O0013004468WABCOGRP000001", "DINET                    ", ISO_8859_1);

		assertTrue(compare(expected, modified) == 0);

	}

}
