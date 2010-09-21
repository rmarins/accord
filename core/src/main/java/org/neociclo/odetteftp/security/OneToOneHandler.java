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
package org.neociclo.odetteftp.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;

/**
 * Define the handler for a specific Callback type.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public interface OneToOneHandler<T extends Callback> {

	/**
	 * 
	 * @param cb
	 * @throws IOException
	 *             if an input or output error occur.
	 */
	void handle(T cb) throws IOException;

}
