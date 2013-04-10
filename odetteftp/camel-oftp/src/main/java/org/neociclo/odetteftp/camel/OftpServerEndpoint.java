/**
 * The Accord Project, http://accordproject.org
 * Copyright (C) 2005-2013 Rafael Marins, http://rafaelmarins.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neociclo.odetteftp.camel;

import java.util.Observable;
import java.util.concurrent.ExecutorService;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.Service;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.SynchronousDelegateProducer;
import org.jboss.netty.util.Timer;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 */
public class OftpServerEndpoint extends DefaultEndpoint implements Service, IOftpEndpoint {

	private static final boolean IS_SINGLETON = true;

	private static final String BOSS_THREAD_ID = "OftpServerBossExecutor";
	private static final String WORKER_THREAD_ID = "OftpServerWorkerExecutor";

	private static final Logger LOGGER = LoggerFactory.getLogger(OftpServerEndpoint.class);

	private ExecutorService bossExecutor;
	private ExecutorService workerExecutor;
    private Timer timer;

	private OftpOperations operations;
	private OftpSettings settings;

    private OftpServerConsumer consumer;
	private OftpBinding binding;

	private Observable outgoingRequestsObservable;

	public OftpServerEndpoint(String endpointUri, OftpComponent component, OftpSettings settings) {
		super(endpointUri, component);
		this.settings = settings;
		this.operations = new OftpOperations(this);
	}

	public OftpSettings getSettings() {
		return settings;
	}

	public OftpOperations getOperations() {
		return operations;
	}

	public void setSettings(OftpSettings configuration) {
		this.settings = configuration;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public ExecutorService getBossExecutor() {
		return bossExecutor;
	}

	public void setBossExecutor(ExecutorService executor) {
		this.bossExecutor = executor;
	}

	public ExecutorService getWorkerExecutor() {
		return workerExecutor;
	}

	public void setWorkerExecutor(ExecutorService executor) {
		this.workerExecutor = executor;
	}

	public boolean isSingleton() {
		return IS_SINGLETON;
	}

	public Producer createProducer() throws Exception {

		LOGGER.trace("Creating the Odette FTP server producer");

		// create the OFTP producer
        Producer producer = new DefaultOftpProducer(this);

		LOGGER.debug("Odette FTP server producer created: {}", producer);

        if (isSynchronous()) {
            return new SynchronousDelegateProducer(producer);
        } else {
            return producer;
        }

	}

	public Consumer createConsumer(Processor processor) throws Exception {

		LOGGER.trace("Creating the Odette FTP server consumer");

		if (consumer != null) {
			LOGGER.warn("...............................................................");
			LOGGER.warn("Overriding the existing Odette FTP consumer: {}", consumer);
			LOGGER.warn("...............................................................");
		}

		consumer = new OftpServerConsumer(this, processor);

		LOGGER.debug("Odette FTP server consumer created: {}", consumer);

		return consumer;

	}

	public void start() throws Exception {

		if (bossExecutor != null) {
			LOGGER.trace("Creating Odette FTP server endpoint Boss executor"); 
			bossExecutor = getCamelContext().getExecutorServiceStrategy().newThreadPool(this, BOSS_THREAD_ID,
					settings.getCorePoolSize(), settings.getMaxPoolSize());
			LOGGER.debug("Odette FTP server endpoint Boss executor created: {}", bossExecutor);
		}

		if (workerExecutor != null) {
			LOGGER.trace("Creating Odette FTP server endpoint Worker executor"); 
			workerExecutor = getCamelContext().getExecutorServiceStrategy().newThreadPool(this, WORKER_THREAD_ID,
					settings.getCorePoolSize(), settings.getMaxPoolSize());
			LOGGER.debug("Odette FTP server endpoint Worker executor created: {}", workerExecutor);
		}

		//
		// Create server producer onProcess observer
		//
		if (outgoingRequestsObservable == null) {
			// create an auto-changeable Observable on notifyObservers()
			outgoingRequestsObservable = new Observable() {
				public synchronized void addObserver(java.util.Observer o) {
					LOGGER.trace("addObserver(): {} - {}", o, this);
					super.addObserver(o);
				};
				public void notifyObservers(Object arg) {
					LOGGER.trace("countObservers: {} - {}", countObservers(), this);
					setChanged();
					super.notifyObservers(arg);
				};
			};
		}

		super.start();
	}

	public void stop() throws Exception {
//		operations.awaitDisconnect();

		// Release external resources allocated
		if (bossExecutor != null) {
			LOGGER.trace("Shutting down Odette FTP server endpoint Boss executor: {}", bossExecutor);
			getCamelContext().getExecutorServiceStrategy().shutdown(bossExecutor);
		}

		if (workerExecutor != null) {
			LOGGER.trace("Shutting down Odette FTP server endpoint Worker executor: {}", workerExecutor);
			getCamelContext().getExecutorServiceStrategy().shutdown(workerExecutor);
		}

		// Release server producer onProcess observer
		outgoingRequestsObservable = null;

	}

	@Override
	public Exchange createExchange(ExchangePattern pattern) {
		Exchange e = super.createExchange(pattern);
		e.setProperty(Exchange.BINDING, getBinding());
		return e;
	}

	public Exchange createExchange(ExchangePattern pattern, OdetteFtpObject obj, OdetteFtpSession session) {
		return OftpEndpointUtil.createExchange(this, pattern, obj, session);
	}

	public OftpBinding getBinding() {
		if (binding == null) {
			binding = new OftpBinding(this);
		}
		return binding;
	}

	/**
	 * Sets the binding used to convert from a Camel message to and from a OftpMessage
	 * @param binding the binding to set
	 */
	public void setBinding(OftpBinding binding) {
		this.binding = binding;
	}

	public Observable getOutgoingRequestsObservable() {
		return outgoingRequestsObservable;
	}

	public void setOutgoingRequestsObservable(Observable serverProducerOnExchange) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Setting server producer's process observable on the Odette FTP endpoint: {}", this);
		}
		outgoingRequestsObservable = serverProducerOnExchange;
	}

	public OftpServerConsumer getConsumer() {
		return consumer;
	}

}
