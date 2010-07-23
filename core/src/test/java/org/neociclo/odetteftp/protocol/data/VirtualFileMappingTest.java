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
package org.neociclo.odetteftp.protocol.data;

import static org.junit.Assert.*;
import static org.neociclo.odetteftp.protocol.data.AbstractMapping.LINE_SEPARATOR;
import static org.neociclo.odetteftp.util.OftpTestUtil.*;
import static org.neociclo.odetteftp.protocol.DataExchangeBuffer.SubrecordHeader.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Date;

import org.junit.Test;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class VirtualFileMappingTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualFileMappingTest.class);

    public void mappingRead(String path, RecordFormat format, int recordSize, int debSize, boolean useCompression, String marker) throws Exception {

        File payload = getResourceFile(path);
        File copyTo = File.createTempFile(marker + "-", ".test", getOutputDir());

        DefaultVirtualFile virtualFile = new DefaultVirtualFile();
        virtualFile.setDatasetName("PAYLOAD");
        virtualFile.setDateTime(new Date(payload.lastModified()));
        virtualFile.setRecordFormat(format);
        virtualFile.setRecordSize(recordSize);

        FileInputStream inStream = new FileInputStream(payload);
        FileChannel inFileChannel = inStream.getChannel();

        FileOutputStream outStream = new FileOutputStream(copyTo, false);
        FileChannel outFileChannel = outStream.getChannel();

        DataExchangeBuffer deb = new DataExchangeBuffer(debSize);

        AbstractMapping mapping = AbstractMapping.getInstance(OdetteFtpVersion.OFTP_V14, useCompression, format);

        boolean eof;

        long t0 = System.currentTimeMillis();

        do {

            /* Read data buffer from the stream. */
            eof = mapping.readData(virtualFile, inFileChannel, deb);

            
            ByteBuffer dataBuffer = deb.getRawBuffer();
            writeDataToFile(outFileChannel, dataBuffer, virtualFile.getRecordFormat());
            

        } while (!eof);

        long t1 = System.currentTimeMillis();

        LOGGER.trace("{} (compression={}): {}", new Object[] { marker, useCompression, (t1 - t0) });

        try {
            inStream.close();
        } catch (Throwable t) {
            // do nothing
        }

        try {
            outStream.close();
        } catch (Throwable t) {
            // do nothing
        }

        byte[] payloadHash = SecurityUtil.computeFileHash(payload, "MD5");
        byte[] copiedHash = SecurityUtil.computeFileHash(copyTo, "MD5");

        if (copyTo.exists()) {
            copyTo.delete();
        }

        assertTrue(Arrays.equals(payloadHash, copiedHash));

    }

    @Test
    public void testReadingDataFixed() throws Exception {

        mappingRead("data/AGPLV3", RecordFormat.FIXED, 400, 217, false, "testReadingDataFixed");
        mappingRead("data/AGPLV3", RecordFormat.FIXED, 400, 217, true, "testReadingDataFixed");

    }

    @Test
    public void testReadingDataVariable() throws Exception {

        mappingRead("data/AGPLV3", RecordFormat.VARIABLE, 250, 217, false, "testReadingDataVariable");
        mappingRead("data/AGPLV3", RecordFormat.VARIABLE, 250, 217, true, "testReadingDataVariable");

    }

    @Test
    public void testReadingDataTextfile() throws Exception {

        mappingRead("data/AGPLV3", RecordFormat.TEXTFILE, 0, 217, false, "testReadingDataTextfile");
        mappingRead("data/AGPLV3", RecordFormat.TEXTFILE, 0, 217, true, "testReadingDataTextfile");

    }

    @Test
    public void testReadingDataUnstructure() throws Exception {
    
        mappingRead("data/AGPLV3", RecordFormat.UNSTRUCTURED, 0, 217, false, "testReadingDataUnstructured");
        mappingRead("data/AGPLV3", RecordFormat.UNSTRUCTURED, 0, 217, true, "testReadingDataUnstructured");
    
    }

    private void writeDataToFile(FileChannel out, ByteBuffer data, RecordFormat recordFormat) throws IOException {

        data.position(1);
        while (data.hasRemaining()) {

            short subrecordHeader = (short) (data.get() & 0xff);

            int count = getSubrecordCount(subrecordHeader);
            boolean compressed = isCompressed(subrecordHeader);
            boolean endOfRecord = isEndOfRecord(subrecordHeader);

            byte[] subrecord = new byte[(compressed ? 1 : count)];
            try {
                data.get(subrecord);
            } catch (BufferUnderflowException bue) {
                fail("BufferUnderflowException: " + bue.getMessage());
            }

            ByteBuffer bb = ByteBuffer.wrap(subrecord);
            if (compressed) {
                for (int i=0; i<count; i++) {
                    bb.rewind();
                    out.write(bb);
                }
            } else {
                out.write(bb);
            }

            if (endOfRecord && recordFormat == RecordFormat.TEXTFILE) {
                out.write(ByteBuffer.wrap(LINE_SEPARATOR));
            }

        }
        
    }
}
