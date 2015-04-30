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


/**
 * This is a package visible class that provides support to represent a command
 * format. Command is a sequence of plain fields, which type should be Numeric,
 * Alphanumeric, Binary and format Fixed or Variable.
 * 
 * @author Rafael Marins
 */
public interface CommandFormat {

    /**
     * Command Format is compound of one or more fields. This class represents
     * the Field attributes definition.
     * <p/>
     * Dynamic fields are used on protocol version 2.0 implementation and above
     * to indicate the field size or position is computed according to another
     * defined CommandFormat field. Eg.: The <code>SFNAREAST</code> field size
     * is calculate using <code>SFNAREASL</code> numeric value.
     * <p/>
     * Long filename extension is used in protocol version 1.3 and 1.3
     * implementations to support datasetName longer than 26 octets, but its
     * a proprietary definition in <code>SFID</code> command format (up
     * to 128 octets length).
     */
    public static class Field {

        /**
         * An alphanumeric field of length <i>n</i> octets.
         */
        public static final char ALPHANUMERIC_TYPE = 'X';

        /** 
         *  A carriage return (0x0D or 0x8D)
         */
        public static final char CR_TYPE = 'C';
        
        /**
         * A binary field of length n octets.
         * <p>
         * Numbers encoded as binary are always unsigned and in network byte
         * order.
         */
        public static final char BINARY_TYPE = 'U';

        /** An field of length n octets, encoded using [UTF-8]. */
        public static final char ENCODED_TYPE = 'T';

        /**
         * A field containing fixed values. All allowable values for the field
         * are enumerated in the command definition.
         */
        public static final char FIXED_FORMAT = 'F';

        /**
         * A numeric field of length <i>n</i> octets. This type of attribute
         * contains characters from the following set:
         * 
         * <pre>
         *    The numerals:               0 to 9
         *    The upper case letters:     A to Z
         *    The following special set:  / - . &amp; ( ) space.
         * </pre>
         * 
         * Space is not allowed as an embedded character.
         */
        public static final char NUMERIC_TYPE = '9';

        /**
         * A field with variable values within a defined range. For example the
         * <i>SFIDFSIZ</i> field may contain any integer value between 0000000
         * and 9999999.
         */
        public static final char VARIABLE_FORMAT = 'V';

        private char format;
        private String name;
        private int position;
        private int size;
        private char type;
        private boolean dynamic = false;
        private String lengthFieldName;
        private String positionAfterFieldName;

        public Field(int pos, String name, char format, char type, int size) {
            super();
            position = pos;
            this.name = name;
            this.format = format;
            this.type = type;
            this.size = size;
        }

        public Field(int pos, String name, char format, char type, String lengthFieldName) {
            this(pos, name, format, type, 0);
            this.dynamic = true;
            this.lengthFieldName = lengthFieldName;
        }

        public Field(String posAfterFieldName, String name, char format, char type, int size) {
            this(0, name, format, type, size);
            this.dynamic = true;
            this.positionAfterFieldName = posAfterFieldName;
        }

        public Field(String posAfterFieldName, String name, char format, char type, String lengthFieldName) {
            this(0, name, format, type, 0);
            this.dynamic = true;
            this.lengthFieldName = lengthFieldName;
            this.positionAfterFieldName = posAfterFieldName;
        }

        public char getFormat() {
            return format;
        }

        public String getName() {
            return name;
        }

        public int getPosition() {
            return position;
        }

        public int getSize() {
            return size;
        }

        public char getType() {
            return type;
        }

        public boolean isDynamic() {
            return dynamic;
        }

        public boolean shouldComputeSize() {
            return (isDynamic() && size == 0);
        }

        public boolean shouldComputePosition() {
            return (isDynamic() && position == 0);
        }

        public String getLengthFieldName() {
            return lengthFieldName;
        }

        public String getPositionAfterFieldName() {
            return positionAfterFieldName;
        }

    }

    /**
     * Return a Command Format field definition by its key name.
     * 
     * @param name
     *        Field key name within Command Format.
     * @return Field definition, or <code>null</code> if it does not exist.
     */
    Field getField(String name);

    String[] getFieldNames();

    CommandIdentifier getIdentifier();

    /**
     * @return
     */
    int getSize();

}
