/**
 * Neociclo Accord, Open Source B2B Integration Suite
 * Copyright (C) 2005-2010 Neociclo, http://www.neociclo.com
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
 *
 * $Id$
 */
package org.neociclo.odetteftp.camel;

import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.MultipleConsumersSupport;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.Service;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.camel.impl.SynchronousDelegateProducer;
import org.jboss.netty.util.Timer;
import org.neociclo.odetteftp.service.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpClientEndpoint extends ScheduledPollEndpoint implements Service, IOftpEndpoint,
		MultipleConsumersSupport {

	private static final boolean IS_SINGLETON = true;

	private static final String BOSS_THREAD_ID = "OftpClientBossExecutor";
	private static final String WORKER_THREAD_ID = "OftpClientWorkerExecutor";

	private static final Logger LOGGER = LoggerFactory.getLogger(OftpClientEndpoint.class);

	private ExecutorService bossExecutor;
	private ExecutorService workerExecutor;
    private Timer timer;

	private OftpOperations operations;
	private OftpSettings settings;

	private OftpBinding binding;

	private Observable outgoingRequestsObservable;

	private AtomicBoolean hasOut = new AtomicBoolean();
	private AtomicBoolean hasIn = new AtomicBoolean();

	private Client oftpClient;

	private OftpClientConsumer consumer;

	public OftpClientEndpoint(String endpointUri, Component component, OftpSettings settings) {
		super(endpointUri, component);
		this.settings = settings;
		this.operations = new OftpOperations(this);
	}

	public Client getOftpClient() {
		return oftpClient;
	}

	public void setOftpClient(Client client) {
		this.oftpClient = client;
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

	public void setBossExecutor(ExecutorService bossExecutor) {
		this.bossExecutor = bossExecutor;
	}

	public ExecutorService getWorkerExecutor() {
		return workerExecutor;
	}

	public void setWorkerExecutor(ExecutorService workerExecutor) {
		this.workerExecutor = workerExecutor;
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

	public boolean isSingleton() {
		return IS_SINGLETON;
	}

	public Producer createProducer() throws Exception {

		LOGGER.trace("Creating the Odette FTP client producer");

		// create the OFTP producer
		DefaultOftpProducer producer = new DefaultOftpProducer(this);

		LOGGER.debug("Odette FTP client producer created: {}", producer);

        if (isSynchronous()) {
            return new SynchronousDelegateProducer(producer);
        } else {
            return producer;
        }
	}

	public Consumer createConsumer(Processor processor) throws Exception {

		LOGGER.trace("Creating the Odette FTP client consumer");

		if (consumer != null) {
			LOGGER.warn("...............................................................");
			LOGGER.warn("Overriding the existing Odette FTP consumer: {}", consumer);
			LOGGER.warn("...............................................................");
		}

		// create the OFTP consumer
        consumer = new OftpClientConsumer(this, processor);

		LOGGER.debug("Odette FTP client consumer created: {}", consumer);

		return consumer;
	}

	public boolean setHasIn() {
		synchronized (hasIn) {
			return hasIn.getAndSet(true);
		}
	}

	public boolean hasIn() {
		synchronized (hasIn) {
			return hasIn.get();
		}
	}

	public AtomicBoolean getHasIn() {
		return hasIn;
	}

	public boolean setHasOut() {
		synchronized (hasOut) {
			return hasOut.getAndSet(true);
		}
	}

	public boolean hasOut() {
		synchronized (hasOut) {
			return hasOut.get();
		}
	}

	public AtomicBoolean getHasOut() {
		return hasOut;
	}

	public boolean unsetHasOut() {
		synchronized (hasOut) {
			return hasOut.getAndSet(false);
		}
	}

	public boolean unsetHasIn() {
		synchronized (hasIn) {
			return hasIn.getAndSet(false);
		}
	}

	public Observable getOutgoingRequestsObservable() {
		return outgoingRequestsObservable;
	}

	@Override
	public void start() throws Exception {

		if (bossExecutor != null) {
			LOGGER.trace("Creating Odette FTP client endpoint Boss executor"); 
			bossExecutor = getCamelContext().getExecutorServiceStrategy().newThreadPool(this, BOSS_THREAD_ID,
					settings.getCorePoolSize(), settings.getMaxPoolSize());
			LOGGER.debug("Odette FTP client endpoint Boss executor created: {}", bossExecutor);
		}

		if (workerExecutor != null) {
			LOGGER.trace("Creating Odette FTP client endpoint Worker executor"); 
			workerExecutor = getCamelContext().getExecutorServiceStrategy().newThreadPool(this, WORKER_THREAD_ID,
					settings.getCorePoolSize(), settings.getMaxPoolSize());
			LOGGER.debug("Odette FTP client endpoint Worker executor created: {}", workerExecutor);
		}


		//
		// Create producer onProcess observer
		//
		if (outgoingRequestsObservable == null) {
			// create an auto-changeable Observable on notifyObservers()
			this.outgoingRequestsObservable = new Observable() {
				private Queue<Object> exchangeQueue = new ConcurrentLinkedQueue<Object>();
				public void notifyObservers(Object arg) {
					if (countObservers() == 0) {
						exchangeQueue.offer(arg);
						setHasOut();
						try {
							operations.runClient();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						setChanged();
						super.notifyObservers(arg);
					}
				}
				public synchronized void addObserver(java.util.Observer o) {
					super.addObserver(o);
					while (!exchangeQueue.isEmpty()) {
						setChanged();
						super.notifyObservers(exchangeQueue.poll());
					}
				}
			};
		}

		super.start();
	}

	@Override
	public void stop() throws Exception {

		// Release external resources allocated
		if (bossExecutor != null) {
			LOGGER.trace("Shutting down Odette FTP client endpoint Boss executor: {}", bossExecutor);
			getCamelContext().getExecutorServiceStrategy().shutdown(bossExecutor);
		}

		if (workerExecutor != null) {
			LOGGER.trace("Shutting down Odette FTP client endpoint Worker executor: {}", workerExecutor);
			getCamelContext().getExecutorServiceStrategy().shutdown(workerExecutor);
		}
		
		// Release server producer onProcess observer
		this.outgoingRequestsObservable = null;

		super.stop();
	}

	public boolean isMultipleConsumersSupported() {
		return false;
	}

	public IOftpConsumer getConsumer() {
		return consumer;
	}
}
