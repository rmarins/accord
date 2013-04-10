/**
 * Neociclo Accord, Open Source B2B Integration Suite
 * Copyright (C) 2005-2010 Neociclo, http://www.neociclo.com
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
 *
 * $Id$
 */
package org.neociclo.odetteftp.camel;

import java.io.File;

import org.apache.camel.Exchange;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.spi.Synchronization;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
class DeleteReceivedFile implements Synchronization {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteReceivedFile.class);

	private OftpOperations operations;
	private VirtualFile receivedFile;

	public DeleteReceivedFile(IOftpEndpoint endpoint, VirtualFile receivedFile) {
		super();
		this.operations = endpoint.getOperations();
		this.receivedFile = receivedFile;
	}

	public void onComplete(Exchange exchange) {
		int retries = 3;
		boolean deleted = false;

		File file = receivedFile.getFile();

		while (retries > 0 && !deleted) {
			retries--;

			if (operations.deleteFile(file)) {
				// file is deleted
				deleted = true;
				break;
			}

			// some OS can report false when deleting but the
			// file is still deleted
			// use exists to check instead
			boolean exists = operations.existsFile(file);
			if (!exists) {
				deleted = true;
			} else {
				LOGGER.trace("File was not deleted at this attempt will try again in 1 sec.: {}", file);
				}
				// sleep a bit and try again
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					LOGGER.error("An one sec. nightmare.", e);
				}
			}

		if (!deleted) {
			throw new GenericFileOperationFailedException("Cannot delete file: " + file);
		}
	}

	public void onFailure(Exchange exchange) {
	}

}
