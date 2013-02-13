/**
 * Neociclo Accord, Open Source B2B Integration Suite
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

import org.neociclo.odetteftp.OdetteFtpException;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class EnvelopingException extends OdetteFtpException {

	private static final long serialVersionUID = 1L;

	public EnvelopingException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public EnvelopingException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public EnvelopingException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
