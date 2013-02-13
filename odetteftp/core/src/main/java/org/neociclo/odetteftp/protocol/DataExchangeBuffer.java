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

import static org.neociclo.odetteftp.protocol.CommandIdentifier.DATA;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.util.ByteBufferFactory;

/**
 * DataExchange is an type of Odette FTP Exchange Buffer used for encapsulate
 * and transmit of Virtual File records.
 * <p>
 * For transmission of Virtual File records, data is divided into Subrecords,
 * each of which is preceded by a one octet Subrecord Header.
 * <p>
 * The Data Exchange Buffer is made up of the initial Command character,
 * 
 * <pre>
 *  o--------------------------------------------------------
 *  | C | H |           | H |           | H |           |   /
 *  | M | D | SUBRECORD | D | SUBRECORD | D | SUBRECORD |  /_
 *  | D | R |           | R |           | R |           |   /
 *  o-------------------------------------------------------
 * </pre>
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class DataExchangeBuffer implements OdetteFtpExchangeBuffer {

    public static class SubrecordHeaderIterator implements Iterator<SubrecordHeader> {

        private ByteBuffer buf;

        public SubrecordHeaderIterator(ByteBuffer buffer) {
            super();
            this.buf = buffer;
        }

        public boolean hasNext() {
            return buf.remaining() > 0;
        }

        public SubrecordHeader next() {

            byte headerOctet = buf.get();

            boolean compressed = SubrecordHeader.isCompressed(headerOctet);
            boolean endOfRecord = SubrecordHeader.isEndOfRecord(headerOctet);
            byte count = SubrecordHeader.getSubrecordCount(headerOctet);

            int dataIndex = buf.position();

            return new SubrecordHeader(dataIndex, endOfRecord, compressed, count);
        }

        /**
         * Not supported.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }

    /**
     * Subrecord Header is defined in one octet as follows:
     * 
     * <pre>
     *           0   1   2   3   4   5   6   7
     *         o-------------------------------o
     *         | E | C |                       |
     *         | o | F | C O U N T             |
     *         | R |   |                       |
     *         o-------------------------------o
     *
     *
     *   Bits
     * 
     *    0     End of Record Flag
     * 
     *          Set to indicate that the next subrecord is the last
     *          subrecord of the current record.
     * 
     *          Unstructured files are transmitted as a single record; in
     *          this case, the flag acts as an end-of-file marker.
     * 
     *    1     Compression Flag
     * 
     *          Set to indicate that the next subrecord is compressed.
     * 
     *   2-7    Subrecord Count
     * 
     *          The number of octets in the Virtual File represented by the
     *          next subrecord expressed as a binary value.
     * 
     *          For uncompressed data, this is simply the length of the
     *          subrecord.
     * 
     *          For compressed data, this is the number of times that the
     *          single octet in the following subrecord must be inserted in
     *          the Virtual File.
     * 
     *          As 6 bits are available, the next subrecord may represent
     *          between 0 and 63 octets of the Virtual File.
     * </pre>
     */
    public static class SubrecordHeader {

        /**
         * Subrecord Count: last 6 bits
         * 
         * @param headerOctet
         * @return
         */
        public static byte getSubrecordCount(short headerOctet) {
            return (byte) (headerOctet & 0x3f);
        }

        /**
         * Compression Flag: is second bit set?
         * 
         * @param headerOctet
         * @return
         */
        public static boolean isCompressed(short headerOctet) {
            return ((headerOctet & 0x40) == 0x40);
        }

        /**
         * End of Record: is first bit set?
         * 
         * @param headerOctet
         * @return
         */
        public static boolean isEndOfRecord(short headerOctet) {
            return ((headerOctet & 0x80) == 0x80);
        }

        public static byte toOctet(boolean endOfRecord, boolean compression, byte count) {
            short subrecordHeader = 0;
            subrecordHeader += (endOfRecord ? 0x80 : 0x00);
            subrecordHeader += (compression ? 0x40 : 0x00);
            subrecordHeader += (count & 0x3f);
            return (byte) subrecordHeader;
        }

        private boolean compressed;

        private byte count;

        private boolean endOfRecord;

        int dataIndex;

        /**
         * @param startPos
         *            the Subrecord position within the Data Exchange Buffer
         *            (after the Subrecord header).
         * @param eor
         *            End of Record flag.
         * @param compr
         *            Compression flag.
         * @param count
         *            Subrecord count.
         */
        public SubrecordHeader(int startPos, boolean eor, boolean compr, byte count) {
            super();
            dataIndex = startPos;
            endOfRecord = eor;
            compressed = compr;
            this.count = count;
        }

        public byte asOctet() {
            return toOctet(isEndOfRecord(), isCompressed(), getCount());
        }

        /**
         * The number of octets in the Virtual File represented by the next
         * subrecord expressed as a binary value.
         * <p>
         * For uncompressed data this is simply the length of the subrecord.
         * <p>
         * For compressed data this is the number of times that the single octet
         * in the following subrecord must be inserted in the Virtual File.
         * <p>
         * As six bits are available, the next subrecord may represent between 0
         * and 63 octets of the Virtual File.
         * 
         * @return
         */
        public byte getCount() {
            return count;
        }

        public int getDataIndex() {
            return dataIndex;
        }

        /**
         * Set to indicate that the next subrecord is compressed.
         * 
         * @return
         */
        public boolean isCompressed() {
            return compressed;
        }

        /**
         * Set to indicate that the next subrecord is the last subrecord of the
         * current record.
         * <p>
         * Unstructured files are transmitted as a single record, in this case
         * the flag acts as an end of file marker.
         * 
         * @return
         */
        public boolean isEndOfRecord() {
            return endOfRecord;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer("SubrecordHeader (count: ");
            sb.append(getCount()).append(", compr: ").append(isCompressed()).append(", eor: ").append(isEndOfRecord());
            sb.append(")");
            return sb.toString();
        }

    }

//    public static boolean isCompleteBuffer(ByteBuffer in) {
//
//        LOGGER.trace("isCompleteBuffer()   entering");
//        
//        /*
//         * When only one octet is free in the Data Exchange Buffer it cannot be
//         * filled with a subrecord.
//         */
//        if (in.remaining() <= 1) {
//            LOGGER.trace("isCompleteBuffer()   is complete since there is no buffer remaining");
//            return true;
//        }
//
//        /*
//         * Use parseSubrecordsHeaders() function to determine if the given Data
//         * Exchange Buffer is complete.
//         */
//        SubrecordHeader[] subrecords = parseSubrecordHeaders(in);
//
//        boolean complete = (subrecords != null);
//        LOGGER.trace("isCompleteBuffer()   exiting = {}", complete);
//
//        return complete;
//    }
//
//    /**
//     * @param in
//     * @return array of Subrecord Header object instances determining buffer
//     *         boundaries, or <code>null</code> when buffer is incomplete.
//     */
//    private static SubrecordHeader[] parseSubrecordHeaders(ByteBuffer in) {
//
//        LOGGER.trace("parseSubrecordHeaders()   entering");
//
//        ArrayList<SubrecordHeader> subrecords = new ArrayList<SubrecordHeader>();
//
//        int entryPos = in.position();
//
//        int pos = 1;
//
//        while (in.limit() > pos) {
//
//            /* Get the Subrecord Header octet at the expected position. */
//            short header = in.getShort();
//
//            if (header == 0) {
//                LOGGER.debug("parseSubrecordHeaders() exit loop when header octet is zero");
//                break;
//            }
//
//            boolean endOfRecord = SubrecordHeader.isEndOfRecord(header);
//            boolean compressed = SubrecordHeader.isCompressed(header);
//            byte size = SubrecordHeader.getSubrecordCount(header);
//
////            in.position(pos);
//            LOGGER.trace("parseSubrecordHeaders() pos = {}   size = {}   compressed = {}   buffer = {}", new Object[] {
//                    pos, size, compressed, in});
//
//            /*
//             * Data Exchange Buffer is INCOMPLETE when the expected Subrecord
//             * end position is greater than the buffer limit. Then return null.
//             */
//            int subrecordEnd = pos + (compressed ? 1 : size);
//            if (in.limit() < subrecordEnd) {
//                LOGGER.error("parseSubrecordHeaders()   buffer is incomplete (exit) " );
//                return null;
//            }
//
//            /* Add a new Subrecord Header object instance to the array. */
//            SubrecordHeader shdr = new SubrecordHeader(pos, endOfRecord, compressed, size);
//            subrecords.add(shdr);
//
//            /*
//             * Calculate the next Subrecord Header position within the Data
//             * Exchange Buffer.
//             */
//            pos = subrecordEnd + 1;
//
//        }
//
//        SubrecordHeader[] result = subrecords.toArray(new SubrecordHeader[subrecords.size()]);
//        LOGGER.trace("parseSubrecordHeaders()   exiting {}", Arrays.toString(result));
//
//        in.position(entryPos);
//
//        return result;
//    }

    /** The buffer instance. */
    private ByteBuffer data;
    private int unitCount;

//    /** Determine the subrecords boundaries within this Data Exchange Buffer. */
//    private SubrecordHeader[] subrecordHeaders;

    public DataExchangeBuffer(int dataExchangeBufferSize) {
        super();
        data = ByteBufferFactory.allocate(dataExchangeBufferSize - 1);
        data.order(ByteOrder.BIG_ENDIAN);
    }

    public DataExchangeBuffer(ByteBuffer rawBuffer) {
        super();
        rawBuffer.position(1); // just after command octet
        data = rawBuffer.slice();
        data.order(ByteOrder.BIG_ENDIAN);
    }

    public ByteBuffer getRawBuffer() {

        data.flip();
        data.rewind();

        ByteBuffer rawBuffer = ByteBufferFactory.allocate(data.limit() + 1);
        rawBuffer.put((byte) DATA.getCode());
        rawBuffer.put(data);
        rawBuffer.flip();

        return rawBuffer;
    }

    public CommandIdentifier getIdentifier() throws OdetteFtpException {
        return DATA;
    }

    public int getSize() {
        return data.limit();
    }

    public int availableBytes() {
        return data.remaining();
    }

    public boolean available() {
        return (availableBytes() > 0);
    }

    public void clear() {
        data.clear();
    }

//    public SubrecordHeader[] getSubrecordHeaders() {
//        if (subrecordHeaders == null) {
//            subrecordHeaders = parseSubrecordHeaders(getBuffer());
//            LOGGER.trace("getSubrecordHeaders() method return is {}", subrecordHeaders);
//        }
//
//        return subrecordHeaders;
//    }

    public ByteBuffer readData() {
        data.rewind();
        return data;
    }

    public void writeData(byte[] subrecord, boolean endOfRecord, boolean compression, byte count) {
        data.put(SubrecordHeader.toOctet(endOfRecord, compression, count));
        if (subrecord != null) {
            data.put(subrecord);
        }
    }

    public void writeData(byte subrecord, boolean endOfRecord, boolean compression, byte count) {
        data.put(SubrecordHeader.toOctet(endOfRecord, compression, count));
        data.put(subrecord);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("DataExchangeBuffer (size: ");
        sb.append(getSize()).append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DataExchangeBuffer))
            return false;

        DataExchangeBuffer deb = (DataExchangeBuffer) obj;

        return data.equals(deb.data);
    }

    public void setUnitCount(int octets) {
        this.unitCount = octets;
    }

    public int getUnitCount() {
        return unitCount;
    }

//    public int getDataSize() {
//        if (dataSize == 0) {
//            int total = 0;
//            SubrecordHeader[] headers = getSubrecordHeaders();
//            for (SubrecordHeader sh : headers)
//                total += sh.getCount();
//            setDataSize(total);
//        }
//        return dataSize;
//    }
//
//    public void setDataSize(int realDataSize) {
//        this.dataSize = realDataSize;
//    }
}
