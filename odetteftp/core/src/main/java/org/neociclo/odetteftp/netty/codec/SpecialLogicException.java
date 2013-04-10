/**
 * Neociclo Accord, Open Source B2B Integration Suite
 * Copyright (C) 2005-2011 Neociclo, http://www.neociclo.com
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
