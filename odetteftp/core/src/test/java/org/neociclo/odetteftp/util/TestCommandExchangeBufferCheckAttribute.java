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
package org.neociclo.odetteftp.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.CommandFormat.Field;

/**
 * @author Rafael Marins
 */
public class TestCommandExchangeBufferCheckAttribute {

	@Test
    public void testCheckAttribute() {
        assertTrue(CommandExchangeBuffer.checkAttribute(Field.ALPHANUMERIC_TYPE, "ALPHA12345"));
        assertTrue(CommandExchangeBuffer.checkAttribute(Field.NUMERIC_TYPE, "12345"));
        
        assertFalse(CommandExchangeBuffer.checkAttribute(Field.ALPHANUMERIC_TYPE, "ALPH@!23 5"));
        assertFalse(CommandExchangeBuffer.checkAttribute(Field.NUMERIC_TYPE, "12B4A"));
    }

}
