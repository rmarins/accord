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
 * @version $Rev$ $Date$
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
