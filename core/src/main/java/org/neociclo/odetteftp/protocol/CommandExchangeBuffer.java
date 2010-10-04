/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.neociclo.odetteftp.protocol.CommandFormat.Field;
import org.neociclo.odetteftp.util.ByteBufferFactory;
import org.neociclo.odetteftp.util.ProtocolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Command Exchange Buffer contains a single command starting at the beginning
 * of the buffer. Commands and data are never mixed within an Exchange Buffer.
 * Each command has a fixed length and can not be compressed.
 * <p/>
 * In protocol version 2.0 implementation the CommandExchangeBuffer use
 * DynamicField feature to compute the field size and/or positioning.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class CommandExchangeBuffer implements OdetteFtpExchangeBuffer {

    /**
     * Default charset defined in Odette FTP protocol specification.
     */
    public static final Charset DEFAULT_PROTOCOL_CHARSET = Charset.forName("ISO_646.IRV:1991");

    /**
     * UTF-8 charset encoding used in text description in the new OFTP 2.0.
     */
    public static final Charset UTF8_ENCODED_PROTOCOL_CHARSET = Charset.forName("UTF-8");

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExchangeBuffer.class);

    public static String formatAttribute(Field field, String value) {

        String result = null;

        char type = field.getType();
        int length = field.getSize();

        checkAttribute(field.getType(), value);

        if (type == Field.ALPHANUMERIC_TYPE) {
            if (value != null && value.length() > length) {
                // truncate
                LOGGER.warn("Truncating field [{}] with length value of [{}] greater than {}.",
                        new Object[] { field.getName(), value, length });
                result = value.substring(0, length);
            } else {
                LOGGER.warn("Padding field [{}] with length value of [{}] lower than {}.",
                        new Object[] { field.getName(), value, length });
                // padd with whitespace
                result = ProtocolUtil.padd(value, length, false, ' ');
            }
            
            String upperResult = result.toUpperCase();
            if (!upperResult.equals(result)) {
                LOGGER.warn("Value [{}] has lower case characters. Original value not being changed.");
            }
        } else if (type == Field.NUMERIC_TYPE) {
            if (value.length() < length) {
                LOGGER.warn("Padding numeric field [{}] with length value of [{}] lower than {}.",
                        new Object[] { field.getName(), value, length });
            }
            result = ProtocolUtil.padd(value, length, true, '0');
        } else if (type == Field.ENCODED_TYPE) {
            result = value;
        }

        return result;
    }

    public static boolean checkAttribute(char type, String value) {
        if (value == null) {
            return true;
        }

        if (type == Field.ALPHANUMERIC_TYPE) {
            int length = value.length();
            if (!value.matches("\\p{Alnum}{" + length + "}")) {
                LOGGER.warn("Value [{}] is not ALPHANUMERIC", value);
                return false;
            }

            return true;
        } else if (type == Field.NUMERIC_TYPE) {
            int length = value.length();
            if (!value.matches("\\p{Digit}{" + length + "}")) {
                LOGGER.warn("Value [{}] is not NUMERIC", value);
                return false;
            }

            return true;
        }

        return false;
    }

    private Map<String, Object> attributes;
    private Integer size = null;
    private final CommandFormat commandFormat;

    public CommandExchangeBuffer(CommandFormat commandFormat) {
        super();
        this.commandFormat = commandFormat;
        attributes = new HashMap<String, Object>();
    }

    /**
     * Return a Command Exchange Buffer parameter value for the specified field.
     * 
     * @param name
     *            Field key name.
     * @return Corresponding parameter value for a given field.
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public String getStringAttribute(String name) {
        return (String) getAttribute(name);
    }

    public byte[] getByteArrayAttribute(String name) {
        return (byte[]) getAttribute(name);
    }

    public String getFormattedAttribute(String name) {

        Field field = commandFormat.getField(name);
        String value = getStringAttribute(name);

        return formatAttribute(field, value);
    }

    public ByteBuffer getRawBuffer() {

        ByteBuffer buffer = ByteBufferFactory.allocate(getSize());
        buffer.order(ByteOrder.BIG_ENDIAN);

        /*
         * Loop through all command fields and put the formatted field value
         * within the buffer on its specified position. Event though a field is
         * not present in the command attributes set (often as Reserved are), it
         * will be formatted with blank spaces value.
         */
        for (String fieldName : getFieldNames()) {

            Field field = commandFormat.getField(fieldName);

            byte[] octets = null;

            if (field.getType() == Field.BINARY_TYPE) {
                octets = getByteArrayAttribute(fieldName);
            } else {
                String text = formatAttribute(field, getStringAttribute(fieldName));

                if (text == null) {
                    continue;
                }

                if (field.getType() == Field.ENCODED_TYPE) {
                    octets = text.getBytes(UTF8_ENCODED_PROTOCOL_CHARSET);
                } else {
                    octets = text.getBytes(DEFAULT_PROTOCOL_CHARSET);
                }
            }

            if (octets == null && LOGGER.isDebugEnabled())
                LOGGER.debug("getBuffer() - Field is null: {}", fieldName);

            if (octets != null) {
                buffer.put(octets);
            }
        }

        buffer.flip();

        return buffer;
    }

    /**
     * @return Array of strings representing attributes names.
     */
    public String[] getFieldNames() {
        return commandFormat.getFieldNames();
    }

    public CommandIdentifier getIdentifier() {
        return commandFormat.getIdentifier();
    }

    public int getSize() {

        if (size == null) {
            int sum = 0;
            for (String fieldName : commandFormat.getFieldNames()) {
                Field field = commandFormat.getField(fieldName);
                if (field.shouldComputeSize() && attributes.containsKey(fieldName)) {
                    Object attr = getAttribute(fieldName);
                    if (attr instanceof String) {
                        sum += ((String) attr).length();
                    } else if (attr instanceof byte[]) {
                        sum += ((byte[]) attr).length;
                    }
                } else {
                    sum += field.getSize();
                }
            }
            size = sum;
        }

        return size;
    }

    public Object setAttribute(String fieldName, Object value) {
        return attributes.put(fieldName, value);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getIdentifier()).append("[");
        String[] names = getFieldNames();
        for (int i = 0; i < names.length; i++) {
            String fieldName = names[i];
            Object value = getAttribute(fieldName);
            sb.append(fieldName.toLowerCase()).append(": ").append(value);
            if (i < (names.length - 1))
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CommandExchangeBuffer))
            return false;
        CommandExchangeBuffer cmd = (CommandExchangeBuffer) obj;

        for (String fieldName : getFieldNames()) {
            Object value = getAttribute(fieldName);
            Object compareValue = cmd.getAttribute(fieldName);
            if (value != null && compareValue != null && !value.equals(compareValue))
                return false;
        }

        return true;
    }
}
