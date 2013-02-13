package org.neociclo.odetteftp.util;

import junit.framework.TestCase;

import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.CommandFormat.Field;

public class TestCommandExchangeBufferCheckAttribute extends TestCase {

    public void testCheckAttribute() {
        assertTrue(CommandExchangeBuffer.checkAttribute(Field.ALPHANUMERIC_TYPE, "ALPHA12345"));
        assertTrue(CommandExchangeBuffer.checkAttribute(Field.NUMERIC_TYPE, "12345"));
        
        assertFalse(CommandExchangeBuffer.checkAttribute(Field.ALPHANUMERIC_TYPE, "ALPH@!23 5"));
        assertFalse(CommandExchangeBuffer.checkAttribute(Field.NUMERIC_TYPE, "12B4A"));
    }

}
