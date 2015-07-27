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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neociclo.odetteftp.util.OftpTestUtil.getResourceFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;

import org.junit.Test;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.RecordFormat;

/**
 * @author Gary Barker
 */
public class ExchangeBufferTest {

	public String mappingRead(String path, RecordFormat format, int recordSize, int debSize, boolean useCompression) throws Exception {

		File payload = getResourceFile(path);

		DefaultVirtualFile virtualFile = new DefaultVirtualFile();
		virtualFile.setDatasetName("PAYLOAD");
		virtualFile.setDateTime(new Date(payload.lastModified()));
		virtualFile.setRecordFormat(format);
		virtualFile.setRecordSize(recordSize);

		FileInputStream inStream = new FileInputStream(payload);
		FileChannel inFileChannel = inStream.getChannel();

		DataExchangeBuffer deb = new DataExchangeBuffer(debSize);

		AbstractMapping mapping = AbstractMapping.getInstance(OdetteFtpVersion.OFTP_V20, useCompression, format);

		boolean eof;

		StringBuilder hexString = new StringBuilder();
		do {	 
			/* Read data buffer from the stream. */
			eof = mapping.readData(virtualFile, inFileChannel, deb);
			ByteBuffer dataBuffer = deb.getRawBuffer();
			while (dataBuffer.hasRemaining()) {
				hexString.append(String.format("%02X", dataBuffer.get()));
			}

		} while (!eof);   

		try {
			inStream.close();
		} catch (Throwable t) {
			// do nothing
		}

		return hexString.toString();
	}

	@Test
	public void testFixedUncompressed() throws Exception {
		String hexDataExchangeBuffer = mappingRead("data/TEXTFILE_small", RecordFormat.FIXED, 5, 217, false);
		assertEquals("44853132333435853132333435853535353535853535353535853535353536", hexDataExchangeBuffer);
		hexDataExchangeBuffer = mappingRead("data/TEXTFILE_small", RecordFormat.FIXED, 10, 217, false);
		assertEquals("448A313233343531323334358A35353535353535353535853535353536", hexDataExchangeBuffer);
		hexDataExchangeBuffer = mappingRead("data/TEXTFILE_small", RecordFormat.FIXED, 20, 217, false);
		assertEquals("44943132333435313233343535353535353535353535853535353536", hexDataExchangeBuffer);
		hexDataExchangeBuffer = mappingRead("data/TEXTFILE_small", RecordFormat.FIXED, 40, 217, false);
		assertEquals("449931323334353132333435353535353535353535353535353536", hexDataExchangeBuffer);
		// Test a 00 padding
		hexDataExchangeBuffer = mappingRead("data/TEXTFILE_small", RecordFormat.FIXED, 11, 5, false);
		assertEquals("440331323344033435314403323334448235350044033535354403353535440335353544823535004483353536", hexDataExchangeBuffer);
	}
	
	@Test
	public void testUnstructuredUncompressed() throws Exception {
		String hexDataExchangeBuffer = mappingRead("data/TEXTFILE_small", RecordFormat.UNSTRUCTURED, 5, 217, false);
		assertEquals("449931323334353132333435353535353535353535353535353536", hexDataExchangeBuffer);
		hexDataExchangeBuffer = mappingRead("data/TEXTFILE_10x32", RecordFormat.UNSTRUCTURED, 5, 128, false);
		System.out.println("buffer [" + hexDataExchangeBuffer);
		if (hexDataExchangeBuffer.endsWith("0D0A")) {
			assertEquals("6F729B65646174610D0A6D6F7265646174610D0A6D6F7265646174610D0A", hexDataExchangeBuffer.substring(hexDataExchangeBuffer.length() - 60));
		} else {
			assertEquals("74610A6D6F7265646174610A6D6F7265646174610A6D6F7265646174610A", hexDataExchangeBuffer.substring(hexDataExchangeBuffer.length() - 60));
		}
	}
	
	@Test
	public void testFixedCompressed() throws Exception {
		String hexDataExchangeBuffer = mappingRead("data/TEXTFILE_small", RecordFormat.FIXED, 5, 217, true);
		assertEquals("44853132333435853132333435C535C53544358136", hexDataExchangeBuffer);
		hexDataExchangeBuffer = mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.FIXED, 5, 217, true);
		assertTrue(hexDataExchangeBuffer.startsWith("44C563C563C563C563C5638563636331320133C463C563C563C563C563") );
	}
	
	@Test
	public void testTextCompressed() throws Exception {
		String hexDataExchangeBuffer = mappingRead("data/TEXTFILE_small", RecordFormat.TEXTFILE, 0, 217, true);
		assertEquals("44093132333435313233344F358136", hexDataExchangeBuffer);
		hexDataExchangeBuffer = mappingRead("data/TEXTFILE_small", RecordFormat.TEXTFILE, 0, 10, true);
		assertEquals("440831323334353132334401344F358136", hexDataExchangeBuffer);
		hexDataExchangeBuffer = mappingRead("data/FIXED_PAYLOAD_2176", RecordFormat.TEXTFILE, 0, 217, true);
		assertEquals("445C63033132337F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F637F6383636363", hexDataExchangeBuffer);
	}
	
	@Test
	public void testUnstructuredCompressed() throws Exception {
		String hexDataExchangeBuffer = mappingRead("data/TEXTFILE_small", RecordFormat.UNSTRUCTURED, 0, 128, true);
		assertEquals("44093132333435313233344F358136", hexDataExchangeBuffer);
	}
	
	@Test
	public void testTextUncompressed() throws Exception {
		String hexDataExchangeBuffer = mappingRead("data/TEXTFILE_lineseperators", RecordFormat.TEXTFILE, 0, 10, false);
		if (hexDataExchangeBuffer.startsWith("4408310D0A")) {
			// using 0D0A as line feeds
			assertEquals("4408310D0A320D0A330D44080A340D0A350D0A3644080D0A370D0A380D0A4408390D0A0D0A0D0A0D44840A300D0A", hexDataExchangeBuffer);
		} else {
			// using 0A as line feeds
			assertEquals("4408310A320A330A340A4408350A360A370A380A4487390A0A0A0A300A", hexDataExchangeBuffer);
		}
		hexDataExchangeBuffer = mappingRead("data/TEXTFILE_small", RecordFormat.TEXTFILE, 0, 217, false);
		assertEquals("449931323334353132333435353535353535353535353535353536", hexDataExchangeBuffer);
	}

	
}
