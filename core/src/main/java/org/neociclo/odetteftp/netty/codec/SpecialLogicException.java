/**
 * Neociclo Accord, Open Source B2B Integration Suite
 * Copyright (C) 2005-2011 Neociclo, http://www.neociclo.com
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
package org.neociclo.odetteftp.netty.codec;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelException;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class SpecialLogicException extends ChannelException {

	private static final long serialVersionUID = 1L;

	private ChannelBuffer buffer;

	public SpecialLogicException(ChannelBuffer buffer) {
		super();
		this.buffer = buffer;
	}

	public SpecialLogicException(String message, Throwable cause, ChannelBuffer buffer) {
		super(message, cause);
		this.buffer = buffer;
	}

	public SpecialLogicException(String message, ChannelBuffer buffer) {
		super(message);
		this.buffer = buffer;
	}

	public SpecialLogicException(Throwable cause, ChannelBuffer buffer) {
		super(cause);
		this.buffer = buffer;
	}

	public ChannelBuffer getBuffer() {
		return buffer;
	}

}
