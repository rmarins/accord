package org.neociclo.accord.camel.odette;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.camel.util.ObjectHelper;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.VirtualFile;

public class OdetteEndpoint extends ScheduledPollEndpoint {

	private OdetteOperations operations;
	private OdetteConfiguration configuration;
	private Set<OdetteConsumer> consumers = new HashSet<OdetteConsumer>();

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
		// OdetteProducer odetteProducer = new OdetteProducer(this, operations);
		// producers.add(odetteProducer);
		return null;// odetteProducer;
	}

	public Exchange createExchange(GenericFile<DefaultVirtualFile> file) {
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
		GenericFile<DefaultVirtualFile> file = new GenericFile<DefaultVirtualFile>();
		file.setBody(incomingFile);
		file.setFileLength(incomingFile.getSize());
		file.setFileName(incomingFile.getFile().getName());

		for (OdetteConsumer c : consumers) {
			c.processOdetteMessage(file);
		}
	}

	/**
	 * Configures the given message with the file which sets the body to the
	 * file object.
	 */
	public void configureMessage(GenericFile<DefaultVirtualFile> file, Message message) {
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
	}

	private char getFileSeparator() {
		return File.separatorChar;
	}

}