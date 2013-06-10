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

import static org.junit.Assert.assertTrue;
import static org.neociclo.odetteftp.util.OftpTestUtil.getOutputDir;
import static org.neociclo.odetteftp.util.OftpTestUtil.getResourceFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

		if (copyTo.exists()) {
			copyTo.delete();
		}

		assertTrue(Arrays.equals(payloadHash, copiedHash));

	}

	@Test
	public void testReadingSmallDataFixed() throws Exception {
		mappingRead("data/TEXTFILE_small", RecordFormat.FIXED, 5, 217, false, "testReadingSmallDataFixed");
		mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.FIXED, 10, 217, true, "testReadingSmallDataFixed");

	}

	@Test
	public void testReadingDataFixed() throws Exception {
		mappingRead("data/TEXTFILE", RecordFormat.FIXED, 400, 217, false, "testReadingDataFixed");
		mappingRead("data/TEXTFILE", RecordFormat.FIXED, 400, 217, true, "testReadingDataFixed");

	}

	@Test
	public void testReadingDataFixedPayload() throws Exception {    
		mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.FIXED, 128, 128, false, "testReadingDataFixedPayload");
		mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.FIXED, 128, 128, true, "testReadingDataFixedPayload");       

	}

	@Test
	public void testReadingDataVariable() throws Exception {
		mappingRead("data/TEXTFILE", RecordFormat.VARIABLE, 250, 217, false, "testReadingDataVariable");
		mappingRead("data/TEXTFILE", RecordFormat.VARIABLE, 250, 217, true, "testReadingDataVariable");

	}

	@Test
	public void testReadingDataTextfile() throws Exception {
		mappingRead("data/TEXTFILE", RecordFormat.TEXTFILE, 0, 217, false, "testReadingDataTextfile");
		mappingRead("data/TEXTFILE", RecordFormat.TEXTFILE, 0, 217, true, "testReadingDataTextfile");
		mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.TEXTFILE, 0, 512, false, "testReadingDataTextfile");
		mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.TEXTFILE, 0, 217, true, "testReadingDataTextfile");   

	}

	@Test
	public void testReadingDataUnstructure() throws Exception {
		mappingRead("data/TEXTFILE", RecordFormat.UNSTRUCTURED, 0, 217, false, "testReadingDataUnstructured");
		mappingRead("data/TEXTFILE", RecordFormat.UNSTRUCTURED, 0, 217, true, "testReadingDataUnstructured");
		mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.UNSTRUCTURED, 0, 1920, false, "testReadingDataUnstructured");
		mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.UNSTRUCTURED, 0, 1920, true, "testReadingDataUnstructured");   
	}
}
