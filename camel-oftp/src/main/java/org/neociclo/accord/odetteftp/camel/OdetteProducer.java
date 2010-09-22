package org.neociclo.accord.odetteftp.camel;

import java.io.File;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.protocol.v20.DefaultEnvelopedVirtualFile;
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
		Exchange oftpExchange = endpoint.createExchange(exchange);

		Message in = oftpExchange.getIn();
		DeliveryNotification bodyAsDN = in.getBody(DeliveryNotification.class);
		File bodyAsFile = in.getBody(File.class);
		if (bodyAsDN == null && bodyAsFile == null) {
			throw new RuntimeCamelException("Invalid exchange body. Neither File or DeliveryNotification");
		}

		if (bodyAsDN != null) {
			processDeliveryNotification(oftpExchange);
		} else {
			processFile(oftpExchange);
		}

		ExchangeHelper.copyResults(exchange, oftpExchange);
	}

	private void processDeliveryNotification(Exchange exchange) {
		if (log.isTraceEnabled()) {
			log.trace("Processing " + exchange);
		}

		Message in = exchange.getIn();
		DeliveryNotification dn = in.getBody(DeliveryNotification.class);
		operations.offer(dn);
	}

	private void processFile(Exchange exchange) {
		if (log.isTraceEnabled()) {
			log.trace("Processing " + exchange);
		}

		exchange.setPattern(ExchangePattern.InOut);

		Message in = exchange.getIn();
		VirtualFile virtualFile = in.getBody(VirtualFile.class);
		if (virtualFile == null) {
			File file = in.getBody(File.class);
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

		// if we did copy to temporary file before send, let Odette Operations
		// know about
		if (tempTarget != null) {
			endpoint.getOdetteOperations().notifyOfTemporaryFile(virtualFile);
		}

		// lets store the name we really used in the header, so end-users
		// can retrieve it
		exchange.getIn().setHeader(Exchange.FILE_NAME_PRODUCED, target);
	}

	private File createTempFile(File target) {
		String suffix = ".outgoing";
		File tempTarget = new File(endpoint.getConfiguration().getWorkpath(), target.getName() + suffix);
		return tempTarget;
	}

	private void sendFile(Exchange exchange, VirtualFile virtualFile, File target) {
		exchange.getIn().setHeader(OdetteEndpoint.ODETTE_SOURCE_FILE, virtualFile.getFile());
		((DefaultVirtualFile) virtualFile).setFile(target);

		operations.offer(virtualFile, exchange);
	}

	private VirtualFile convertToVirtualFile(File payload, Message m) {
		String hDatasetName = m.getHeader(OdetteEndpoint.ODETTE_DATASET_NAME, payload.getName(), String.class);
		String hOriginator = m.getHeader(OdetteEndpoint.ODETTE_ORIGINATOR, null, String.class);
		String hDestination = m.getHeader(OdetteEndpoint.ODETTE_DESTINATION, null, String.class);
		RecordFormat hRecordFormat = m.getHeader(OdetteEndpoint.ODETTE_RECORD_FORMAT, RecordFormat.UNSTRUCTURED,
				RecordFormat.class);
		Integer hRecordSize = m.getHeader(OdetteEndpoint.ODETTE_RECORD_SIZE, OdetteFtpConstants.DEFAULT_RECORD_SIZE,
				Integer.class);
		Long hRestartOffset = m.getHeader(OdetteEndpoint.ODETTE_RESTART_OFFSET, 0, Long.class);
		String hFileDescription = m.getHeader(OdetteEndpoint.ODETTE_FILE_DESCRIPTION, String.class);

		DefaultEnvelopedVirtualFile v = new DefaultEnvelopedVirtualFile();
		v.setFile(payload);
		v.setDatasetName(hDatasetName);
		v.setOriginator(hOriginator);
		v.setDestination(hDestination);
		v.setRecordFormat(hRecordFormat);
		v.setRecordSize(hRecordSize);
		v.setRestartOffset(hRestartOffset);
		v.setFileDescription(hFileDescription);

		return v;
	}

}
