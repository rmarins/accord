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
package org.neociclo.odetteftp.protocol;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class DefaultNormalizedVirtualFile extends DefaultVirtualFile implements NormalizedVirtualFile {

	private static final long serialVersionUID = 1L;

	private VirtualFile originalVirtualFile;

	public DefaultNormalizedVirtualFile(VirtualFile originalVirtualFile) {
		super();
		this.originalVirtualFile = originalVirtualFile;
	}

	public VirtualFile getOriginalVirtualFile() {
		return originalVirtualFile;
	}

}
