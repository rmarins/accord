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
package org.neociclo.odetteftp.netty.codec;

import static org.neociclo.odetteftp.protocol.CommandExchangeBuffer.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.CommandFormat;
import org.neociclo.odetteftp.protocol.CommandFormat.Field;
import org.neociclo.odetteftp.util.ProtocolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class CommandExchangeBufferBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandExchangeBufferBuilder.class);

    private static class ParamsRead {
        public ParamsRead(int pos, int size) {
            this.position = pos;
            this.size = size;
        }
        private int position;
        private int size;
    }

    public static CommandExchangeBuffer create(CommandFormat format, ChannelBuffer in) {

        CommandExchangeBuffer command = new CommandExchangeBuffer(format);

        Map<String, ParamsRead> fieldsRead = new HashMap<String, ParamsRead>();

        int bufferLength = in.capacity();

        for (String fieldName : format.getFieldNames()) {

            Field field = format.getField(fieldName);

            int pos = computePosition(field, fieldsRead);
            int size = computeSize(field, fieldsRead, in);

            fieldsRead.put(fieldName, new ParamsRead(pos, size));

            if (size == 0) {
                // empty field - skip
                continue;
            } else if ((bufferLength - pos) < size) {
                // error - no remaining buffer to read field value
                return command;
            }

            byte[] octets = new byte[size];
            in.getBytes(pos, octets);

            // raw bytes
            if (field.getType() == Field.BINARY_TYPE) {
                command.setAttribute(fieldName, octets);
            }
            // UTF-8 encoded text
            else if (field.getType() == Field.ENCODED_TYPE) {
                String encodedText = null;
				try {
					encodedText = new String(octets, UTF8_ENCODED_PROTOCOL_CHARSET);
				} catch (UnsupportedEncodingException e) {
					LOGGER.error("Cannot encode " + UTF8_ENCODED_PROTOCOL_CHARSET + " protocol parameter: " + fieldName, e);
					continue;
				}

				command.setAttribute(fieldName, encodedText);

            }
            // alphanumeric text
            else {

                String text = null;
				try {
					text = new String(octets, DEFAULT_PROTOCOL_CHARSET);
				} catch (UnsupportedEncodingException e) {
					LOGGER.error("Cannot encode " + DEFAULT_PROTOCOL_CHARSET + " protocol parameter: " + fieldName, e);
					continue;
				}

                if (field.getType() == Field.ALPHANUMERIC_TYPE) {
                    // carriage return fields are settled as alphanumeric and shouldn't be trimmed
                    if (!"\r".equals(text) && !"\n".equals(text))
                        text = text.trim();
                } else if (field.getType() == Field.NUMERIC_TYPE) {
                    text = text.trim();
                }

                command.setAttribute(field.getName(), text);

            }
            
        }

        return command;
    }

    private static int computeSize(Field field, Map<String, ParamsRead> fieldsRead, ChannelBuffer in) {

        int size = 0;

        if (field.shouldComputeSize()) {

            String lengthField = field.getLengthFieldName();
            ParamsRead params = fieldsRead.get(lengthField);

            // Compute binary value of field length when the target field type is Binary
            if (field.getType() == Field.BINARY_TYPE) {
                byte[] bin = new byte[params.size];
                in.getBytes(params.position, bin);
                size = ProtocolUtil.parseBinaryNumber(bin);
            } else {
                ChannelBuffer buf = ChannelBuffers.buffer(params.size);
                in.getBytes(params.position, buf);
                String lengthValue = buf.toString(DEFAULT_PROTOCOL_CHARSET);
                size = Integer.parseInt(lengthValue);
            }

        } else {
            size = field.getSize();
        }

        return size;
    }

    private static int computePosition(Field field, Map<String, ParamsRead> fieldsRead) {

        int pos = 0;

        if (field.shouldComputePosition()) {
            String placeAfterField = field.getPositionAfterFieldName();
            ParamsRead params = fieldsRead.get(placeAfterField);
            pos = params.position + params.size;
        } else {
            pos = field.getPosition();
        }

        return pos;
    }

}
