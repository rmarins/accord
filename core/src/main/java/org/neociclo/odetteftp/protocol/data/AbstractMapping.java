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

import static org.neociclo.odetteftp.protocol.CommandExchangeBuffer.DEFAULT_PROTOCOL_CHARSET;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.protocol.VirtualFileMappingException;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public abstract class AbstractMapping implements MappingStrategy {

    /**
     * Default file block length in octets.
     */
    public static final int DEFAULT_VIRTUAL_FILE_BLOCK_SIZE = 1024;

    public static final int TEXTFILE_BLOCK_SIZE = 2048;

    public static final int MAX_SUBRECORD_LENGTH = 63;

    protected static final byte[] LINE_SEPARATOR = getProtocolEncodedBytes(System.getProperty("line.separator"));

    private static AbstractMapping compressionMappingSingleton;

    private static AbstractMapping normalMappingSingleton;

    public static AbstractMapping getInstance(OdetteFtpVersion version, boolean compression, RecordFormat recordFormat) {
        if (compression) {
            if (compressionMappingSingleton == null)
                compressionMappingSingleton = new CompressionMapping();
            return compressionMappingSingleton;
        } else {
            if (normalMappingSingleton == null)
                normalMappingSingleton = new NormalMapping();
            return normalMappingSingleton;
        }
    }

    private static byte[] getProtocolEncodedBytes(String text) {
        byte[] encoded;
        encoded = text.getBytes(DEFAULT_PROTOCOL_CHARSET);
        return encoded;
    }

    public abstract boolean readData(VirtualFile virtualFile, FileChannel fileChannel, DataExchangeBuffer dataBuffer)
            throws OdetteFtpException;

    public abstract long writeData(VirtualFile virtualFile, DataExchangeBuffer dataBuffer, FileChannel fileChannel)
            throws OdetteFtpException;

//    protected ByteBuffer readRecord(FileChannel in, IVirtualFile virtualFile) throws OdetteFtpException {
//
//        /* Determine record size according to the Virtual File format. */
//        int recordSize = DEFAULT_VIRTUAL_FILE_BLOCK_SIZE;
//        if (virtualFile.getRecordFormat() == TEXTFILE)
//            recordSize = TEXTFILE_BLOCK_SIZE;
//        else if (((virtualFile.getRecordFormat() == VARIABLE) || (virtualFile.getRecordFormat() == FIXED)) && virtualFile.getRecordSize() > 0)
//            recordSize = virtualFile.getRecordSize();
//
//        long entryPos;
//
//        try {
//            entryPos = in.position();
//        } catch (IOException e) {
//            throw new VirtualFileMappingException("Cannot determine Virtual File current position: " + virtualFile, e);
//        }
//
//        long recordLimit = ((entryPos / recordSize) + 1) * recordSize;
//        int allocateSize = (int) (recordLimit - entryPos);
//
//        if (allocateSize <= 0)
//            return null;
//
//        ByteBuffer buffer = ByteBufferFactory.allocate(allocateSize);
//
//        try {
//            int bytesRead = in.read(buffer);
//            if (bytesRead == -1) {
//                return null;
//            }
//            buffer.flip();
//        } catch (IOException e) {
//            throw new VirtualFileMappingException("Cannot read record from the input Virtual File at " + entryPos + ": "
//                    + virtualFile, e);
//        }
//
//        try {
//            in.position(entryPos);
//        } catch (IOException e) {
//            throw new VirtualFileMappingException("Cannot discard read bytes on the input Virtual back to " +
//                    entryPos + ": " + virtualFile, e);
//        }
//
//        return (buffer.limit() == 0 ? null : buffer);
//    }


    protected long position(FileChannel in) throws VirtualFileMappingException {
        try {
            return in.position();
        } catch (IOException e) {
            throw new VirtualFileMappingException("Cannot determine Virtual File current position.", e);
        }
    }

    protected void skip(FileChannel in, int bytes) throws VirtualFileMappingException {

        try {
            long entryPos = in.position();
            in.position(entryPos + bytes);
        } catch (IOException e) {
            throw new VirtualFileMappingException("Cannot skip reading bytes on the input Virtual back: "
                    + bytes, e);
        }

    }

    protected void discardReadBytes(FileChannel in, int discardedBytes) throws VirtualFileMappingException {

        try {
            long entryPos = in.position();
            in.position(entryPos - discardedBytes);
        } catch (IOException e) {
            throw new VirtualFileMappingException("Cannot discard read bytes on the input Virtual back: "
                    + discardedBytes, e);
        }
        
    }

    protected int read(FileChannel in, ByteBuffer buffer) throws VirtualFileMappingException {
        int bytesRead;
        try {
            buffer.clear();
            bytesRead = in.read(buffer);
            buffer.flip();
        } catch (IOException e) {
            throw new VirtualFileMappingException("Cannot read from the input Virtual File.", e);
        }
        return bytesRead;
    }

}
