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
package org.neociclo.odetteftp.protocol.data;

import static org.neociclo.odetteftp.protocol.RecordFormat.TEXTFILE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.protocol.VirtualFileMappingException;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer.SubrecordHeader;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer.SubrecordHeaderIterator;
import org.neociclo.odetteftp.util.BufferUtil;
import org.neociclo.odetteftp.util.ByteBufferFactory;
import org.neociclo.odetteftp.util.ProtocolUtil;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class CompressionMapping extends AbstractMapping {

    private static final int COMPRESSION_MIN_SEQUENCE_LENGTH = 3;

    @Override
    public boolean readData(VirtualFile virtualFile, FileChannel in, DataExchangeBuffer deb) throws OdetteFtpException {

        boolean eof = false;

        int totalBytesRead = 0;

        /*
         * Reset the DataExchangeBuffer data. Begin at position 1 (after the
         * DATA command identifier octet.
         */
        deb.clear();

        /*
         * Loop until the Data Exchange Buffer is fulfilled (and there are still
         * some space available). Drain data stream of read records into Data
         * Exchange Buffer subrecords.
         */
        int freeSpace;
        ByteBuffer buffer = ByteBufferFactory.allocate(virtualFile.getRecordFormat() == TEXTFILE ? MAX_SUBRECORD_LENGTH + 1: MAX_SUBRECORD_LENGTH);
        while ((freeSpace = deb.availableBytes()) > 0) {

            boolean endOfRecord = false;
            byte[] subrecord = null;

            long entryPosition = position(in);
            int bytesRead = read(in, buffer);

            // reached the end of stream
            if (bytesRead == -1) {
                eof = true;
                break;
            }

            int subrecordSize = Math.min(Math.min(bytesRead, MAX_SUBRECORD_LENGTH), freeSpace - 1);

            // Repetition sequence found within next Subrecord boundaries.
            int posRepetition = BufferUtil.seekRepeatSequence(buffer, subrecordSize, COMPRESSION_MIN_SEQUENCE_LENGTH);

            // on TEXTFILE convert lineSeparator when to set the endOfRecord flag
            if (virtualFile.getRecordFormat() == TEXTFILE) {
                int posLineSep = BufferUtil.seekWithinBuffer(LINE_SEPARATOR, buffer, subrecordSize);
                if (posLineSep != -1) {
                    subrecordSize = posLineSep;
                    endOfRecord = (posRepetition > posLineSep || posRepetition == -1);
                }
            }
            // determine end of record 
            else if (virtualFile.getRecordFormat() != RecordFormat.UNSTRUCTURED && entryPosition > 0) {
                long currentOffset = ProtocolUtil.computeVirtualFileOffset(entryPosition, virtualFile.getRecordFormat(), virtualFile.getRecordSize());
                long recordLimit = virtualFile.getRecordSize() * (currentOffset + 1);

                if (entryPosition + subrecordSize > recordLimit) {
                    subrecordSize = (int) (subrecordSize - (entryPosition + subrecordSize - recordLimit));
                    endOfRecord = true;
                }
            }

            /*
             * Subrecord length to before the repetition sequence start
             * position.
             */
            if (posRepetition != -1 && !endOfRecord) {
                subrecordSize = posRepetition;
            }

            // read buffer into subrecord array
            subrecord = new byte[subrecordSize];
            buffer.get(subrecord);

            if (bytesRead < buffer.capacity()) {
                eof = true;
            }

            // discard additional octets read
            if (subrecordSize < bytesRead) {
                discardReadBytes(in, (bytesRead - subrecordSize));
                eof = false;
            }

            /*
             * When line separator is found and handled above, skip the line
             * separator length octets on reading record. Thus it will rewind
             * the remaining bytes left and re-read the record and continue the
             * loop while (until DEB is full or End Of Stream is reached).
             */
            if ((virtualFile.getRecordFormat() == TEXTFILE) && endOfRecord) {
                skip(in, LINE_SEPARATOR.length);
            }

            /*
             * Unstructured files are transmitted as a single record; in this
             * case, the flag acts as an end-of-file marker.
             */
            if (virtualFile.getRecordFormat() == RecordFormat.UNSTRUCTURED && eof) {
                endOfRecord = true;
            }

            // need compression
            if (posRepetition != -1 && subrecordSize == 0 && (virtualFile.getRecordFormat() == TEXTFILE && !endOfRecord)) {
              buffer.position(posRepetition);
              byte repeatOctet = buffer.get(posRepetition);
              int count = BufferUtil.getRepeatSequenceCount(buffer, MAX_SUBRECORD_LENGTH);

              skip(in, count);

              deb.writeData(repeatOctet, endOfRecord, true, (byte) count);

              totalBytesRead += count;

            } else {
                /*
                 * Append the Header and Subrecord into the Data Exchange Buffer.
                 * Even when it's a null sized subrecord.
                 */
                deb.writeData(subrecord, endOfRecord, false, (byte) (subrecordSize & 0xff ));
                totalBytesRead += subrecordSize;

            }

        }

        deb.setUnitCount(totalBytesRead);

        return eof;
    }

    @Override
    public long writeData(VirtualFile virtualFile, DataExchangeBuffer deb, FileChannel out)
            throws OdetteFtpException {

        int bytesWritten = 0;

        if (out == null) {
            throw new NullPointerException("fileChannel");
        }

        ByteBuffer data = deb.getRawBuffer();
        Iterator<SubrecordHeader> subrecordHeaders = new SubrecordHeaderIterator(data);

        try {
            while (subrecordHeaders.hasNext()) {
    
                SubrecordHeader header = subrecordHeaders.next();
    
                if (header.getCount() > 0) {

                    // read the subrecord
                    byte[] subrecord = new byte[header.getCount()];
                    data.get(subrecord);

                    // write down to the output file channel
                    ByteBuffer buf = ByteBuffer.wrap(subrecord);

                    if (header.isCompressed()) {
                        for (;; header.getCount()) {
                            buf.rewind();
                            out.write(buf);
                        }
                    } else {
                        out.write(buf);
                    }

                    bytesWritten += header.getCount();

                }
    
                /*
                 * Handle the endOfRecord flag when Virtual File is TEXTFILE format
                 * to add line separator at the end of each line/record.
                 */
                if (header.isEndOfRecord() && virtualFile.getRecordFormat() == TEXTFILE) {
                    out.write(ByteBuffer.wrap(LINE_SEPARATOR));
                    bytesWritten += LINE_SEPARATOR.length;
                }
    
            }
    
            out.force(false);
        } catch (IOException e) {
            throw new VirtualFileMappingException("Write data operation failed.", e);
        }

        deb.setUnitCount(bytesWritten);

        return bytesWritten;
    }

}
