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
package org.neociclo.odetteftp.protocol;

import static org.junit.Assert.*;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.ENCODED_TYPE;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.FIXED_FORMAT;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.NUMERIC_TYPE;
import static org.neociclo.odetteftp.protocol.CommandFormat.Field.VARIABLE_FORMAT;
import static org.neociclo.odetteftp.util.CommandFormatConstants.*;

import org.junit.Test;
import org.neociclo.odetteftp.protocol.CommandFormat.Field;

/**
 * @author Rafael Marins
 */
public class FieldTest {

    @Test
    public void testFixedPositionAndSizeField() {

        // Start File: original file size field format
        Field sfidosiz = new Field(125, SFIDOSIZ_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 13);

        assertEquals("Bad field position value.", 125, sfidosiz.getPosition());
        assertEquals("Bad field size value.", 13, sfidosiz.getSize());
        assertEquals("Bad field name value.", SFIDOSIZ_FIELD, sfidosiz.getName());
        assertFalse("Bad dynamic field configuration. Should return false!", sfidosiz.isDynamic());
        assertFalse("Bad should compute position value. Should return false!", sfidosiz.shouldComputePosition());
        assertFalse("Bad should compute size value. Should return false!", sfidosiz.shouldComputeSize());
        assertNull("Bad length field name value.", sfidosiz.getLengthFieldName());
        assertNull("Bad position after field name value.", sfidosiz.getPositionAfterFieldName());

    }

    @Test
    public void testDynamicSizeField() {
        
        // Start File: Virtual File description field format.
        Field sfiddesc = new Field(165, SFIDDESC_FIELD, VARIABLE_FORMAT, ENCODED_TYPE, SFIDDESCL_FIELD);

        assertEquals("Bad field position value.", 165, sfiddesc.getPosition());
        assertEquals("Bad field size value.", 0, sfiddesc.getSize());
        assertEquals("Bad field name value.", SFIDDESC_FIELD, sfiddesc.getName());
        assertTrue("Bad dynamic field configuration. Should return true!", sfiddesc.isDynamic());
        assertFalse("Bad should compute position value. Should return false!", sfiddesc.shouldComputePosition());
        assertTrue("Bad should compute size value. Should return true!", sfiddesc.shouldComputeSize());
        assertEquals("Bad length field name value.", SFIDDESCL_FIELD, sfiddesc.getLengthFieldName());
        assertNull("Bad position after field name value. Should return null.", sfiddesc.getPositionAfterFieldName());

    }

    @Test
    public void testDynamicPositioningField() {

        // Negative End Response: Virtual File hash length field format. 
        Field nerprest = new Field(NERPREAST_FIELD, NERPHSHL_FIELD, VARIABLE_FORMAT, NUMERIC_TYPE, 2);

        assertEquals("Bad field position value.", 0, nerprest.getPosition());
        assertEquals("Bad field size value.", 2, nerprest.getSize());
        assertEquals("Bad field name value.", NERPHSHL_FIELD, nerprest.getName());
        assertTrue("Bad dynamic field configuration. Should return true!", nerprest.isDynamic());
        assertTrue("Bad should compute position value. Should return true!", nerprest.shouldComputePosition());
        assertFalse("Bad should compute size value. Should return false!", nerprest.shouldComputeSize());
        assertNull("Bad length field name value. Should return null.", nerprest.getLengthFieldName());
        assertEquals("Bad position after field name value.", NERPREAST_FIELD, nerprest.getPositionAfterFieldName());

    }

    @Test
    public void testDynamicPositioningAndSizeField() {
        
        // Negative End Response: Virtual File hash field format.
        Field nerphshl = new Field(NERPHSHL_FIELD, NERPHSH_FIELD, FIXED_FORMAT, NUMERIC_TYPE, "NERPHSHL");

        assertEquals("Bad field position value.", 0, nerphshl.getPosition());
        assertEquals("Bad field size value.", 0, nerphshl.getSize());
        assertEquals("Bad field name value.", NERPHSH_FIELD, nerphshl.getName());
        assertTrue("Bad dynamic field configuration. Should return true!", nerphshl.isDynamic());
        assertTrue("Bad should compute position value. Should return true!", nerphshl.shouldComputePosition());
        assertTrue("Bad should compute size value. Should return true!", nerphshl.shouldComputeSize());
        assertEquals("Bad length field name value.", NERPHSHL_FIELD, nerphshl.getLengthFieldName());
        assertEquals("Bad position after field name value.", NERPHSHL_FIELD, nerphshl.getPositionAfterFieldName());

    }
}
