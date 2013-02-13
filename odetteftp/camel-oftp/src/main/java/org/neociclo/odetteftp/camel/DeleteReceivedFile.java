/**
 * Neociclo Accord, Open Source B2B Integration Suite
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
