package org.neociclo.accord.camel.odette;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.ScheduledPollEndpoint;

public class OdetteEndpoint extends ScheduledPollEndpoint {

	private OdetteOperations operations;
	private OdetteConfiguration configuration;

	public OdetteEndpoint(String uri, OdetteComponent component,
			OdetteConfiguration configuration) {
		super(uri, component);

		operations = new OdetteOperations(this);

		this.configuration = configuration;
	}

	public OdetteConfiguration getConfiguration() {
		return configuration;
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		return new OdetteConsumer(this, processor, operations);
	}

	public Producer createProducer() throws Exception {
		return new OdetteProducer(this, operations);
	}

	public boolean isSingleton() {
		return true;
	}

	public OdetteOperations getOdetteOperations() {
		return operations;
	}

}