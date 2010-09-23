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
package org.neociclo.accord.odetteftp.camel;

import java.io.File;

import org.apache.camel.Exchange;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.camel.spi.Synchronization;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

final class OdetteOnFileReceived implements Synchronization {

	private static final transient Log log = LogFactory.getLog(ScheduledPollConsumer.class);
	private final GenericFile<File> file;
	private OdetteOperations operations;

	public OdetteOnFileReceived(OdetteOperations operations, GenericFile<File> file) {
		this.operations = operations;
		this.file = file;
	}

	public void onComplete(Exchange exchange) {
		int retries = 3;
		boolean deleted = false;

		while (retries > 0 && !deleted) {
			retries--;

			if (operations.deleteFile(file.getAbsoluteFilePath())) {
				// file is deleted
				deleted = true;
				break;
			}

			// some OS can report false when deleting but the
			// file is still deleted
			// use exists to check instead
			boolean exits = operations.existsFile(file.getAbsoluteFilePath());
			if (!exits) {
				deleted = true;
			} else {
				if (log.isTraceEnabled()) {
					log.trace("File was not deleted at this attempt will try again in 1 sec.: " + file);
				}
				// sleep a bit and try again
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		if (!deleted) {
			throw new GenericFileOperationFailedException("Cannot delete file: " + file);
		}
	}

	public void onFailure(Exchange exchange) {
	}
}