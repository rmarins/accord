/**
 * Neociclo Accord, Open Source B2Bi Middleware
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

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 *
 */
public class SpecialLogicDecoder extends FrameDecoder {

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {

		int offset = buffer.readerIndex();

		boolean startsWithSTX = (buffer.getByte(offset) & 0x02) == 0x02;
		if (!startsWithSTX) {
			throw new SpecialLogicException("STX expected at buffer's begin. But was found: 0x"
					+ Integer.toHexString(buffer.getByte(offset)), buffer);
		}

		boolean containsCR = (lastIndexOf(0x0d, buffer) != -1);
		if (!containsCR) {
			return null;
		}

		buffer.skipBytes(1); // skip STX and BSN

		// check BSN is as expected
		int bsn = buffer.readByte() & 0xff;
		int expected = computeBlockSequenceNumber(ctx);
		if (bsn != expected) {
			buffer.readerIndex(offset); // discard bytes read
			throw new SpecialLogicException("Unexpected block sequence number: " + bsn, buffer);
		}

		int L = buffer.readableBytes();

		ChannelBuffer plainBuffer = dynamicBuffer(L - 5);

		int c0 = 0;
		int c1 = 0;
		boolean drop = false;
		while (buffer.readable()) {
			byte octet = buffer.readByte();

			// checksum calculation
        	c0 = (c0 + octet) % 255;
        	c1 = (c1 + c0) % 255;

			// shift-out receive logic
			if (drop) {
				plainBuffer.writeByte(octet - 0x20);
			} else if ((octet & 0x0d) == 0x0d || (octet & 0x8d) == 0x8d) {
				break;
			} else if ((octet & 0x0e) == 0x0e || (octet & 0x8e) == 0x8e) {
				drop = true;
			} else {
				plainBuffer.writeByte(octet & 0xff);
			}
		}

		/*
		 * Checksum verification
		 * ---------------------
		 * 
		 * If, when all the octets have been processed, either or both C0 and C1
		 * does not have the value zero, then the checksum formulas have not
		 * been satisfied.
		 */
		boolean checksum = (c0 == 0) && (c1 == 0);
		if (!checksum) {
			buffer.readerIndex(offset); // discard bytes read
			throw new SpecialLogicException("Checksum has failed.", buffer);
		}

		incrementBlockSequenceNumber(ctx);

		return plainBuffer;
	}

	private int incrementBlockSequenceNumber(ChannelHandlerContext ctx) {
		return (getBlockSequenceCounter(ctx).incrementAndGet() % 10);
	}

	private int computeBlockSequenceNumber(ChannelHandlerContext ctx) {
		AtomicInteger blockCounter = getBlockSequenceCounter(ctx);
		return (blockCounter.get() % 10);
	}

	private AtomicInteger getBlockSequenceCounter(ChannelHandlerContext ctx) {
		AtomicInteger blockSequenceCounter = (AtomicInteger) ctx.getAttachment();
		if (blockSequenceCounter == null) {
			blockSequenceCounter = new AtomicInteger(0);
			ctx.setAttachment(blockSequenceCounter);
		}
		return blockSequenceCounter;
	}

	private int lastIndexOf(int value, ChannelBuffer buffer) {

		int offset = buffer.readerIndex();
		int L = buffer.readableBytes();
		for (int i=L; i>=4; i--) {
			int pos = offset + i;
			byte octet = buffer.getByte(pos);
			if ((octet & value) == value) {
				return pos;
			}
		}
		return -1;
	}


}
