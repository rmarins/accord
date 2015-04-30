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
package org.neociclo.odetteftp.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

        if (type == Field.ALPHANUMERIC_TYPE || type == Field.CR_TYPE) {
        	if (value != null && value.length() > length) {
                // truncate
                LOGGER.warn("Truncating field [{}] with length value of [{}] greater than {}.",
                        new Object[] { field.getName(), value, length });
                result = value.substring(0, length);
            } else {
            	if (value != null && value.length() < length) {
            		LOGGER.info("Padding field [{}] with length value of [{}] lower than {}.",
            				new Object[] { field.getName(), value, length });
            	}
                // pad with whitespace
                result = ProtocolUtil.padd(value, length, false, ' ');
            }

        	String upperResult = result.toUpperCase();
        	if (!upperResult.equals(result)) {
        		LOGGER.warn("Value [{}] has lower case characters. Original value not being changed.");
        	}
        } else if (type == Field.NUMERIC_TYPE) {
        	if (value == null || value.length() < length) {
        		LOGGER.debug("Padding numeric field [{}] with length value of [{}] lower than {}.",
        				new Object[] { field.getName(), value, length });
        	}
        	// pad with zeroes
        	result = ProtocolUtil.padd(value, length, true, '0');
        } else if (type == Field.ENCODED_TYPE) {
        	result = value;
        }

        return result;
    }

    public static boolean checkAttribute(char type, String value) {
        if (value == null || value.equals("")) {
            return true;
        }

        if (type == Field.ALPHANUMERIC_TYPE) {
            int length = value.length();
            if (length > 0 && !value.matches("[A-Z0-9/\\-\\.&\\(\\) ]{" + length + "}")) {
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
        } else if (type == Field.CR_TYPE) {
        	int length = value.length();
            if (length != 1 || ! CR_BYTES.contains(value.charAt(0))) {
                LOGGER.warn("Value [{}] is neither an ASCII nor EBCDIC carriage return", value);
                return false;
            }
        }

        return false;
    }

    private Map<String, Object> attributes;
    private Integer size = null;
    private final CommandFormat commandFormat;

    // valid values for the SSRM CR field
	private static final List<Character> CR_BYTES = 
			Arrays.asList(new Character[] { 0x0d, 0x8d} );

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
