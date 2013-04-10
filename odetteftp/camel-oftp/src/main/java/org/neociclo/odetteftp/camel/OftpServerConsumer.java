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

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.util.ObjectHelper;
import org.neociclo.odetteftp.service.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 */
public class OftpServerConsumer extends DefaultConsumer implements IOftpConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(OftpServerConsumer.class);

    private CamelContext context;
	private OftpSettings settings;
	private Server oftpService;
	private OftpOperations operations;

	public OftpServerConsumer(Endpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.context = this.getEndpoint().getCamelContext();
		this.settings = this.getEndpoint().getSettings();
		this.operations = this.getEndpoint().getOperations();
	}

	@Override
	public OftpServerEndpoint getEndpoint() {
		return (OftpServerEndpoint) super.getEndpoint();
	}

	public CamelContext getContext() {
		return context;
	}

	public OftpSettings getSettings() {
		return settings;
	}

	public void setSettings(OftpSettings configuration) {
		this.settings = configuration;
	}

	public Server getOftpService() {
		return oftpService;
	}

	public void setOftpService(Server oftpService) {
		this.oftpService = oftpService;
	}

	@Override
	protected void doStart() throws Exception {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Odette FTP server consumer binding to: {}", settings.getAddressInfo());
		}

		super.doStart();
		ObjectHelper.notNull(settings.getTransport(), "transport", getEndpoint());

		// initialize the oftpServer variable with corresponding transport server
		switch (settings.getTransport()) {
		case TCPIP:
			oftpService = operations.initializeServer();
			break;
		default:
			throw new IllegalArgumentException("Unsupported Odette FTP endpoint transport: " +
					settings.getTransport());
		}

		// bind/listen the OFTP service
		oftpService.start();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Odette FTP server consumer bound to: {}", settings.getAddressInfo());
		}

	}

	@Override
	protected void doStop() throws Exception {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Odette FTP server consumer unbinding from: {}", settings.getAddressInfo());
		}

		super.doStop();

		// end OFTP server from listening for incoming connections
		oftpService.stop();
		oftpService = null;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Odette FTP server consumer unbound from: {}", settings.getAddressInfo());
		}
	}

	public void process(Exchange exchange) {
		try {
			getProcessor().process(exchange);
		} catch (Exception e) {
			getExceptionHandler().handleException(e);
		}
	}

}
