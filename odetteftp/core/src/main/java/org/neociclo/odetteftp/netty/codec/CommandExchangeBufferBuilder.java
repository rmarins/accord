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

import static org.neociclo.odetteftp.protocol.CommandExchangeBuffer.*;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.CommandFormat;
import org.neociclo.odetteftp.protocol.CommandFormat.Field;
import org.neociclo.odetteftp.util.ProtocolUtil;

/**
 * @author Rafael Marins
 */
public class CommandExchangeBufferBuilder {

    private static class ParamsRead {
        public ParamsRead(int pos, int size) {
            this.position = pos;
            this.size = size;
        }
        private int position;
        private int size;
    }

    public static CommandExchangeBuffer create(CommandFormat format, ChannelBuffer in,
    		ChannelBufferFactory bufferFactory) {

        CommandExchangeBuffer command = new CommandExchangeBuffer(format);

        Map<String, ParamsRead> fieldsRead = new HashMap<String, ParamsRead>();

        int bufferLength = in.capacity();

        for (String fieldName : format.getFieldNames()) {

            Field field = format.getField(fieldName);

            int pos = computePosition(field, fieldsRead);
            int size = computeSize(field, fieldsRead, in, bufferFactory);

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
				encodedText = new String(octets, UTF8_ENCODED_PROTOCOL_CHARSET);

				command.setAttribute(fieldName, encodedText);

            }
            // alphanumeric text
            else {

                String text = null;
				text = new String(octets, DEFAULT_PROTOCOL_CHARSET);

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

    private static int computeSize(Field field, Map<String, ParamsRead> fieldsRead, ChannelBuffer in,
    		ChannelBufferFactory bufferFactory) {

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
                ChannelBuffer buf = bufferFactory.getBuffer(params.size);
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
