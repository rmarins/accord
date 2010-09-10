package org.neociclo.accord.camel.odette;

import java.util.HashSet;
import java.util.Set;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.neociclo.odetteftp.protocol.VirtualFile;

public class OdetteEndpoint extends ScheduledPollEndpoint {

	private OdetteOperations operations;
	private OdetteConfiguration configuration;
	private Set<OdetteConsumer> consumers = new HashSet<OdetteConsumer>();
	//private Set<OdetteProducer> producers = new HashSet<OdetteProducer>();

	public OdetteEndpoint(String uri, OdetteComponent component,
			OdetteConfiguration configuration) {
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
		return e;
	}

	public Producer createProducer() throws Exception {
		operations.setHasOutQueue();
	//	OdetteProducer odetteProducer = new OdetteProducer(this, operations);
		//producers.add(odetteProducer);
		return null;//odetteProducer;
	}

	public boolean isSingleton() {
		return true;
	}

	public OdetteOperations getOdetteOperations() {
		return operations;
	}

	public Exchange notifyConsumersOfIncomingFile(VirtualFile incomingFile) {
		return null;
	}

}