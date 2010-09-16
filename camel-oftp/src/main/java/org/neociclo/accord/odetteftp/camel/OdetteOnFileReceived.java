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