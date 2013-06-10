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
        byte[] encoded = null;
		encoded = text.getBytes(DEFAULT_PROTOCOL_CHARSET);
        return encoded;
    }

    public abstract boolean readData(VirtualFile virtualFile, FileChannel fileChannel, DataExchangeBuffer dataBuffer)
            throws OdetteFtpException;

    public abstract long writeData(VirtualFile virtualFile, DataExchangeBuffer dataBuffer, FileChannel fileChannel)
            throws OdetteFtpException;

    protected long position(FileChannel in) throws VirtualFileMappingException {
        try {
            return in.position();
        } catch (IOException e) {
            throw new VirtualFileMappingException("Cannot determine Virtual File current position.", e);
        }
    }

/*    protected void skip(FileChannel in, int bytes) throws VirtualFileMappingException {

        try {
            long entryPos = in.position();
            in.position(entryPos + bytes);
        } catch (IOException e) {
            throw new VirtualFileMappingException("Cannot skip reading bytes on the input Virtual back: "
                    + bytes, e);
        }

    }*/

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
