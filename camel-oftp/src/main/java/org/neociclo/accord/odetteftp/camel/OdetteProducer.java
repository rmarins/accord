package org.neociclo.accord.odetteftp.camel;

import java.io.File;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.file.GenericFileExist;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.VirtualFile;

public class OdetteProducer extends DefaultProducer {

	protected final transient Log log = LogFactory.getLog(getClass());
	private OdetteEndpoint endpoint;
	private OdetteOperations operations;
	private File file;

	public OdetteProducer(Endpoint endpoint, OdetteOperations operations) {
		super(endpoint);
		this.endpoint = (OdetteEndpoint) endpoint;
		this.operations = operations;
	}

	public void process(Exchange exchange) throws Exception {
		Exchange fileExchange = endpoint.createExchange(exchange);
		processExchange(fileExchange);
		ExchangeHelper.copyResults(exchange, fileExchange);
	}

	private void processExchange(Exchange exchange) {
	/*
		if (log.isTraceEnabled()) {
			log.trace("Processing " + exchange);
		}

		Message in = exchange.getIn();
		VirtualFile virtualFile = in.getBody(VirtualFile.class);
		if (virtualFile == null) {
			File file = in.getBody(File.class);
			if (file == null) {
				throw new RuntimeCamelException("No available data on exchange");
			}
			virtualFile = convertToVirtualFile(file, in);
		}

		try {
			String target = virtualFile.getFile().getPath();

			// should we write to a temporary name and then afterwards rename to
			// real target
			boolean writeAsTempAndRename = ObjectHelper.isNotEmpty(endpoint.getConfiguration().isWriteAsTemp());
			String tempTarget = null;
			if (writeAsTempAndRename) {
				// compute temporary name with the temp prefix
				tempTarget = createTempFileName(exchange, target);

				if (log.isTraceEnabled()) {
					log.trace("Writing using tempNameFile: " + tempTarget);
				}

				// cater for file exists option on the real target as
				// the file operations code will work on the temp file

				// delete any pre existing temp file
				if (operations.existsFile(tempTarget)) {
					if (log.isTraceEnabled()) {
						log.trace("Deleting existing temp file: " + tempTarget);
					}
					if (!operations.deleteFile(tempTarget)) {
						throw new GenericFileOperationFailedException("Cannot delete file: " + tempTarget);
					}
				}
			}

			sendFile(exchange, tempTarget != null ? tempTarget : target);

			// if we did write to a temporary name then rename it to the real
			// name after we have written the file
			if (tempTarget != null) {

				// if we should not eager delete the target file then do it now
				// just before renaming
				if (!endpoint.isEagerDeleteTargetFile() && operations.existsFile(target)
						&& endpoint.getFileExist() == GenericFileExist.Override) {
					// we override the target so we do this by deleting it so
					// the temp file can be renamed later
					// with success as the existing target file have been
					// deleted
					if (log.isTraceEnabled()) {
						log.trace("Deleting existing file: " + target);
					}
					if (!operations.deleteFile(target)) {
						throw new GenericFileOperationFailedException("Cannot delete file: " + target);
					}
				}

				// now we are ready to rename the temp file to the target file
				if (log.isTraceEnabled()) {
					log.trace("Renaming file: [" + tempTarget + "] to: [" + target + "]");
				}
				boolean renamed = operations.renameFile(tempTarget, target);
				if (!renamed) {
					throw new GenericFileOperationFailedException("Cannot rename file from: " + tempTarget + " to: "
							+ target);
				}
			}

			// lets store the name we really used in the header, so end-users
			// can retrieve it
			exchange.getIn().setHeader(Exchange.FILE_NAME_PRODUCED, target);
		} catch (Exception e) {
			handleFailedWrite(exchange, e);
		}
*/
	}

	private String createTempFileName(Exchange exchange, String target) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * If we fail writing out a file, we will call this method. This hook is
	 * provided to disconnect from servers or clean up files we created (if
	 * needed).
	 */
	protected void handleFailedWrite(Exchange exchange, Exception exception) throws Exception {
		throw exception;
	}

	private void sendFile(Exchange exchange) {
		if (log.isTraceEnabled()) {
			log.trace("Processing " + exchange);
		}

		Message in = exchange.getIn();
		VirtualFile virtualFile = in.getBody(VirtualFile.class);
		if (virtualFile == null) {
			File file = in.getBody(File.class);
			if (file == null) {
				throw new RuntimeCamelException("No available data on exchange");
			}
			virtualFile = convertToVirtualFile(file, in);
		}

		operations.offer(virtualFile);
	}

	private VirtualFile convertToVirtualFile(File payload, Message message) {
		DefaultVirtualFile defaultvf = new DefaultVirtualFile();
		defaultvf.setFile(payload);

		// TODO fill VirtualFile with message headers

		return defaultvf;
	}

}
