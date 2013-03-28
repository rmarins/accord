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
 * $Id: VirtualFileMappingTest.java 923 2012-11-08 06:43:16Z rmarins $
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
import org.neociclo.odetteftp.protocol.DataExchangeBuffer.SubrecordHeader;
import org.neociclo.odetteftp.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev: 923 $ $Date: 2012-11-08 07:43:16 +0100 (jeu., 08 nov. 2012) $
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

        AbstractMapping mapping = AbstractMapping.getInstance(OdetteFtpVersion.OFTP_V12, useCompression, format);

        boolean eof;

        long t0 = System.currentTimeMillis();
        
       

        do {	 
            /* Read data buffer from the stream. */
            eof = mapping.readData(virtualFile, inFileChannel, deb);
            ByteBuffer dataBuffer = deb.getRawBuffer();
            DataExchangeBuffer outBuffer = new DataExchangeBuffer(dataBuffer);
            mapping.writeData(virtualFile, outBuffer, outFileChannel);
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

//        if (copyTo.exists()) {
//            copyTo.delete();
//        }

        assertTrue(Arrays.equals(payloadHash, copiedHash));

    }

    @Test
    public void testReadingDataFixed() throws Exception {
        mappingRead("data/AGPLV3", RecordFormat.FIXED, 400, 217, false, "testReadingDataFixed");
        mappingRead("data/AGPLV3", RecordFormat.FIXED, 400, 217, true, "testReadingDataFixed");

    }

    @Test
    public void testReadingDataFixedPayload() throws Exception {    
        mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.FIXED, 128, 128, false, "testReadingDataFixedPayload");
        mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.FIXED, 128, 128, true, "testReadingDataFixedPayload");       

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
        mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.TEXTFILE, 0, 512, false, "testReadingDataTextfile");
        mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.TEXTFILE, 0, 217, true, "testReadingDataTextfile");   

    }

    @Test
    public void testReadingDataUnstructure() throws Exception {
        mappingRead("data/AGPLV3", RecordFormat.UNSTRUCTURED, 0, 217, false, "testReadingDataUnstructured");
        mappingRead("data/AGPLV3", RecordFormat.UNSTRUCTURED, 0, 217, true, "testReadingDataUnstructured");
        mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.UNSTRUCTURED, 0, 1920, false, "testReadingDataUnstructured");
        mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.UNSTRUCTURED, 0, 1920, true, "testReadingDataUnstructured");   
    }
}
