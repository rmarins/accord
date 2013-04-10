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
package org.neociclo.odetteftp.examples.proxy;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferIndexFinder;

/**
 * @author Rafael Marins
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
