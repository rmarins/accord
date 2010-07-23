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
package org.neociclo.odetteftp.util;

import java.io.Serializable;
import java.util.Map;

/**
 * A key that makes its parent {@link Map} or session attribute to search
 * fast while being debug-friendly by providing the string representation.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public final class AttributeKey implements Serializable {
    /** The serial version UID */
    private static final long serialVersionUID = -583377473376683096L;
    
    /** The attribute's name */
    private final String name;

    /**
     * Creates a new instance. It's built from :
     * - the class' name
     * - the attribute's name
     * - this attribute hashCode
     */
    public AttributeKey(Class<?> source, String name) {
        this.name = source.getName() + '.' + name + '@' + Integer.toHexString(this.hashCode());
    }

    /**
     * The String representation of tis objection is its constructed name.
     */
    @Override
    public String toString() {
        return name;
    }
}
