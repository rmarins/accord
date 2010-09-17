package org.neociclo.accord.odetteftp.camel;

import java.io.File;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.util.OdetteFtpConstants;

public class OdetteProducer extends DefaultProducer {

	protected final transient Log log = LogFactory.getLog(getClass());
	private OdetteEndpoint endpoint;
	private OdetteOperations operations;

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

		File target = virtualFile.getFile();
		File tempTarget = null;

		boolean copyBeforeSend = endpoint.getConfiguration().isCopyBeforeSend();
		if (copyBeforeSend) {
			// compute temporary name with the temp prefix
			tempTarget = createTempFile(target);

			if (log.isTraceEnabled()) {
				log.trace("Writing using tempNameFile: " + target);
			}

			// cater for file exists option on the real target as
			// the file operations code will work on the temp file

			// delete any pre existing temp file
			if (tempTarget.exists()) {
				if (log.isTraceEnabled()) {
					log.trace("Deleting existing temp file: " + target);
				}
				if (!operations.deleteFile(tempTarget.getPath())) {
					throw new GenericFileOperationFailedException("Cannot delete file: " + tempTarget);
				}
			}

			operations.storeTempFile(target, tempTarget, exchange);
		}

		sendFile(exchange, virtualFile, tempTarget != null ? tempTarget : target);

		// if we did copy to temporary file before send, let Odette Operations know about
		if (tempTarget != null) {
			endpoint.getOdetteOperations().notifyOfTemporaryFile(virtualFile);
		}

		// lets store the name we really used in the header, so end-users
		// can retrieve it
		exchange.getIn().setHeader(Exchange.FILE_NAME_PRODUCED, target);
	}

	private File createTempFile(File target) {
		String suffix = ".outgoing";
		File tempTarget = new File(endpoint.getConfiguration().getTmpDir(), target.getName() + suffix);
		return tempTarget;
	}

	/**
	 * If we fail writing out a file, we will call this method. This hook is
	 * provided to disconnect from servers or clean up files we created (if
	 * needed).
	 */
	protected void handleFailedWrite(Exchange exchange, Exception exception) throws Exception {
		throw exception;
	}

	private void sendFile(Exchange exchange, VirtualFile virtualFile, File target) {
		exchange.getIn().setHeader(OdetteEndpoint.ODETTE_SOURCE_FILE, virtualFile.getFile());
		((DefaultVirtualFile) virtualFile).setFile(target);
		operations.offer(virtualFile);
	}

	private VirtualFile convertToVirtualFile(File payload, Message message) {
		DefaultVirtualFile defaultvf = new DefaultVirtualFile();
		defaultvf.setFile(payload);

		defaultvf
				.setDatasetName(message.getHeader(OdetteEndpoint.ODETTE_DATASET_NAME, payload.getName(), String.class));
		defaultvf.setOriginator(message.getHeader(OdetteEndpoint.ODETTE_ORIGINATOR, null, String.class));
		defaultvf.setDestination(message.getHeader(OdetteEndpoint.ODETTE_DESTINATION, null, String.class));
		defaultvf.setRecordFormat(message.getHeader(OdetteEndpoint.ODETTE_RECORD_FORMAT, RecordFormat.UNSTRUCTURED,
				RecordFormat.class));
		defaultvf.setRecordSize(message.getHeader(OdetteEndpoint.ODETTE_RECORD_SIZE,
				OdetteFtpConstants.DEFAULT_RECORD_SIZE, Integer.class));
		defaultvf.setRestartOffset(message.getHeader(OdetteEndpoint.ODETTE_RESTART_OFFSET, 0, Long.class));

		return defaultvf;
	}

}
