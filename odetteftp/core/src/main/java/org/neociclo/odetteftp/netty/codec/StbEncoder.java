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
package org.neociclo.odetteftp.netty.codec;

import static org.jboss.netty.buffer.ChannelBuffers.*;
import static org.neociclo.odetteftp.netty.codec.StbConstants.*;

import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * @author Rafael Marins
 */
@Sharable
public class StbEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(
			ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

		ChannelBuffer header = channel.getConfig().getBufferFactory().getBuffer(ByteOrder.BIG_ENDIAN, STB_HEADER_SIZE);
		ChannelBuffer body = (ChannelBuffer) msg;

		if (body.readableBytes() == 0) {
			return wrappedBuffer(body);
		} else {
			int length = STB_HEADER_SIZE + body.readableBytes();
			header.writeByte(STB_V1_NOFLAGS_HEADER);
			header.writeMedium(length);

			return wrappedBuffer(header, body);
		}
	}

}
