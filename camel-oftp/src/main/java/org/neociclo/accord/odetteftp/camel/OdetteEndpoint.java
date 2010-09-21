package org.neociclo.accord.odetteftp.camel;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.Service;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;

public class OdetteEndpoint extends ScheduledPollEndpoint implements Service {

	private static Log log = LogFactory.getLog(OdetteEndpoint.class);

	public static final String ODETTE_SOURCE_FILE = "OdetteSourceFile";
	public static final String ODETTE_ORIGINATOR = "OdetteOriginator";
	public static final String ODETTE_DESTINATION = "OdetteDestination";
	public static final String ODETTE_DATASET_NAME = "OdetteDatasetName";
	public static final String ODETTE_VIRTUAL_FILE = "OdetteVirtualFile";
	public static final String ODETTE_RECORD_FORMAT = "OdetteRecordFormat";
	public static final String ODETTE_RECORD_SIZE = "OdetteRecordSize";
	public static final String ODETTE_RESTART_OFFSET = "OdetteRestartOffset";
	public static final String ODETTE_DATE_TIME = "OdetteDateTime";
	public static final String ODETTE_USER_DATA = "OdetteUserData";
	public static final String ODETTE_DELIVERY_NOTIFICATION = "OdetteDeliveryNotification";

	private OdetteOperations operations;
	private OdetteConfiguration configuration;
	private Set<OdetteConsumer> consumers = new HashSet<OdetteConsumer>();
	private Set<OdetteProducer> producers = new HashSet<OdetteProducer>();

	// private Set<OdetteProducer> producers = new HashSet<OdetteProducer>();

	public OdetteEndpoint(String uri, OdetteComponent component, OdetteConfiguration configuration) {
		super(uri, component);

		this.configuration = configuration;
		this.operations = new OdetteOperations(this);
	}

	public OdetteConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public PollingConsumer createPollingConsumer() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Creating OdettePollingConsumer");
		}

		return super.createPollingConsumer();
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Creating OdetteConsumer");
		}

		operations.setHasInQueue();
		OdetteConsumer e = new OdetteConsumer(this, processor, operations);
		consumers.add(e);
		configureConsumer(e);
		return e;
	}

	public Producer createProducer() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Creating OdetteProducer");
		}

		operations.setHasOutQueue();
		OdetteProducer odetteProducer = new OdetteProducer(this, operations);
		producers.add(odetteProducer);
		return odetteProducer;
	}

	public Exchange createExchange(GenericFile<File> file) {
		Exchange exchange = new DefaultExchange(this);
		if (file != null) {
			file.bindToExchange(exchange);
		}
		return exchange;
	}

	public boolean isSingleton() {
		return true;
	}

	public OdetteOperations getOdetteOperations() {
		return operations;
	}

	/**
	 * Configures the given message with the file which sets the body to the
	 * file object.
	 * 
	 * @param virtualFile
	 */
	public void configureMessage(GenericFile<File> file, VirtualFile virtualFile, Message message) {
		message.setBody(file);

		// compute name to set on header that should be relative to starting
		// directory
		String name = file.isAbsolute() ? file.getAbsoluteFilePath() : file.getRelativeFilePath();

		// skip leading endpoint configured directory
		String endpointPath = getConfiguration().getTmpDir().toString() + getFileSeparator();
		if (ObjectHelper.isNotEmpty(endpointPath) && name.startsWith(endpointPath)) {
			name = ObjectHelper.after(name, endpointPath);
		}

		// adjust filename
		message.setHeader(Exchange.FILE_NAME, name);

		// associate VirtualFile to message header
		configureOdetteMessage(message, virtualFile);
	}

	private void configureOdetteMessage(Message message, VirtualFile virtualFile) {
		message.setHeader(ODETTE_VIRTUAL_FILE, virtualFile);
		configureOdetteMessage(message, (OdetteFtpObject) virtualFile);
	}

	protected void configureOdetteMessage(Message message, DeliveryNotification notif) {
		message.setHeader(ODETTE_DELIVERY_NOTIFICATION, notif);
		configureOdetteMessage(message, (OdetteFtpObject) notif);
	}

	private void configureOdetteMessage(Message message, OdetteFtpObject oftpObject) {
		message.setHeader(ODETTE_ORIGINATOR, oftpObject.getOriginator());
		message.setHeader(ODETTE_DESTINATION, oftpObject.getDestination());
		message.setHeader(ODETTE_DATASET_NAME, oftpObject.getDatasetName());
		message.setHeader(ODETTE_DATE_TIME, oftpObject.getDateTime());
		message.setHeader(ODETTE_USER_DATA, oftpObject.getUserData());
	}

	private char getFileSeparator() {
		return File.separatorChar;
	}

	public void start() throws Exception {

	}

	public void stop() throws Exception {
		operations.awaitDisconnect();
	}

	public void notifyConsumersOf(VirtualFile incomingFile) {
		for (OdetteConsumer c : consumers) {
			c.processOdetteMessage(incomingFile);
		}
	}

	public void notifyConsumersOf(DeliveryNotification notif) {
		for (OdetteConsumer c : consumers) {
			c.processOdetteMessage(notif);
		}
	}

}