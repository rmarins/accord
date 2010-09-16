package org.neociclo.accord.odetteftp.camel;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.ShutdownableService;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.camel.util.ObjectHelper;
import org.neociclo.odetteftp.protocol.VirtualFile;

public class OdetteEndpoint extends ScheduledPollEndpoint implements ShutdownableService {

	private static final String ODETTE_VIRTUAL_FILE = "OdetteVirtualFile";
	private static final String ODETTE_ORIGINATOR = "OdetteOriginator";
	private static final String ODETTE_DESTINATION = "OdetteDestination";
	private static final String ODETTE_DATASET_NAME = "OdetteDatasetName";
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

	public Consumer createConsumer(Processor processor) throws Exception {
		operations.setHasInQueue();
		OdetteConsumer e = new OdetteConsumer(this, processor, operations);
		consumers.add(e);
		configureConsumer(e);
		return e;
	}

	public Producer createProducer() throws Exception {
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

	public void notifyConsumersOfIncomingFile(VirtualFile incomingFile) {
		for (OdetteConsumer c : consumers) {
			c.processOdetteMessage(incomingFile);
		}
	}

	/**
	 * Configures the given message with the file which sets the body to the
	 * file object.
	 * 
	 * @param incomingFile
	 */
	public void configureMessage(GenericFile<File> file, VirtualFile incomingFile, Message message) {
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
		configureOdetteMessage(message, incomingFile);
	}

	private void configureOdetteMessage(Message message, VirtualFile virtualFile) {
		message.setHeader(ODETTE_VIRTUAL_FILE, virtualFile);
		message.setHeader(ODETTE_ORIGINATOR, virtualFile.getOriginator());
		message.setHeader(ODETTE_DESTINATION, virtualFile.getDestination());
		message.setHeader(ODETTE_DATASET_NAME, virtualFile.getDatasetName());
	}

	private char getFileSeparator() {
		return File.separatorChar;
	}

	public void start() throws Exception {
		
	}

	public void stop() throws Exception {
		operations.disconnect();
	}

	public void shutdown() throws Exception {
		operations.disconnect();
	}

}