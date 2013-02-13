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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferIndexFinder;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class ByteArrayIndexFinder implements ChannelBufferIndexFinder {

	private byte[] barray;

	public ByteArrayIndexFinder(byte[] barray) {
		super();
		if (barray == null) {
			throw new NullPointerException("barray");
		}
		this.barray = barray;
	}

	public boolean find(ChannelBuffer buffer, int guessedIndex) {
		for (int i=0; i<barray.length; i++) {
			if (barray[i] != buffer.getByte(guessedIndex + i)) {
				return false;
			}
		}
		return true;
	}

}
