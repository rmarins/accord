/**
 * Neociclo Accord, Open Source B2B Integration Suite
 * Copyright (C) 2005-2012 Neociclo, http://www.neociclo.com
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
