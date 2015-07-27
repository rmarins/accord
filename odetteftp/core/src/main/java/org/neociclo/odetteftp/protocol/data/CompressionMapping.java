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

import static org.neociclo.odetteftp.protocol.RecordFormat.FIXED;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Iterator;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer.SubrecordHeader;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer.SubrecordHeaderIterator;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.protocol.VirtualFileMappingException;
import org.neociclo.odetteftp.util.BufferUtil;
import org.neociclo.odetteftp.util.ByteBufferFactory;
import org.neociclo.odetteftp.util.ProtocolUtil;

/**
 * @author Rafael Marins
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

		long fileSize;
		try {
			fileSize = in.size();
		} catch (IOException e) {
			throw new VirtualFileMappingException("Could not retrieve Virtual File size.", e);
		}

		RecordFormat recordFormat = virtualFile.getRecordFormat();

		/*
		 * Loop until the Data Exchange Buffer is fulfilled (and there are still
		 * some space available). Drain data stream of read records into Data
		 * Exchange Buffer subrecords.
		 */
		int freeSpace;
		ByteBuffer buffer = ByteBufferFactory.allocate(MAX_SUBRECORD_LENGTH);
		byte[] subrecord = null;
		while ((freeSpace = deb.availableBytes()) > 0) {

			boolean endOfRecord = false;

			long entryPosition = position(in);
			int bytesRead = read(in, buffer);

			// reached the end of stream
			if (bytesRead == -1) {
				eof = true;
				break;
			}

			int subrecordSize = Math.min(Math.min(bytesRead, MAX_SUBRECORD_LENGTH), freeSpace - 1);
			if (recordFormat == FIXED) {
				long currentOffset = ProtocolUtil.computeVirtualFileOffset(entryPosition, virtualFile.getRecordFormat(), virtualFile.getRecordSize());
				long recordLimit = virtualFile.getRecordSize() * (currentOffset + 1);

				if (entryPosition + subrecordSize >= recordLimit) {
					subrecordSize = (int) (subrecordSize - (entryPosition + subrecordSize - recordLimit));
					endOfRecord = true;
				}
			}
			// Repetition sequence found within next Subrecord boundaries.
			int posRepetition = BufferUtil.seekRepeatSequence(buffer, subrecordSize, COMPRESSION_MIN_SEQUENCE_LENGTH);
			// need compression
			if (posRepetition == 0) {
				byte repeatOctet = buffer.get(0);
				int count = BufferUtil.getRepeatSequenceCount(buffer, recordFormat == FIXED ? subrecordSize : bytesRead);

				if (count < subrecordSize) {
					endOfRecord = false;
				}
				if (count < bytesRead) {
					discardReadBytes(in, (bytesRead - count));
				}
				if ((entryPosition + count) >= fileSize) {
					eof = true;
					endOfRecord = true;
				}

				deb.writeData(repeatOctet, endOfRecord, true, (byte) count);

				totalBytesRead += count;
			} else {  
				/*
				 * Subrecord length to before the repetition sequence start
				 * position.
				 */
				if (posRepetition > 0) {
					subrecordSize = posRepetition;
					endOfRecord = false;
				}

				// read buffer into subrecord array
				if (subrecord == null || subrecord.length != subrecordSize) {
					subrecord = new byte[subrecordSize];
				} else {
					// total perfumery - since all buffer will be consumed
					Arrays.fill(subrecord, (byte) 0);
				}
				buffer.get(subrecord);

				if ((entryPosition + bytesRead) >= fileSize) {
					eof = true;
				}

				// discard additional octets read
				if (subrecordSize < bytesRead) {
					discardReadBytes(in, (bytesRead - subrecordSize));
					eof = false;
				}

				if (eof) {
					endOfRecord = true;
				}

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


		ByteBuffer data = deb.readData();
		Iterator<SubrecordHeader> subrecordHeaders = new SubrecordHeaderIterator(data);

		try {
			while (subrecordHeaders.hasNext()) {

				SubrecordHeader header = subrecordHeaders.next();
				int count = header.getCount();
				boolean compressed = header.isCompressed();  

				if (count > 0 ) {
					byte[] subrecord = new byte[(compressed ? 1 : count)];
					// read the subrecord
					data.get(subrecord);

					// write down to the output file channel                      
					ByteBuffer bb = ByteBuffer.wrap(subrecord);
					if (compressed) {
						for (int i=0; i<count; i++) {
							bb.rewind();
							out.write(bb);
						}
					} else {
						out.write(bb);
					}

					bytesWritten += count;

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
