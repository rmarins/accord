/**
 * Neociclo Accord, Open Source B2B Integration Suite

 * Copyright (C) 2005-2010 Neociclo, http://www.neociclo.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id$
 */
package org.neociclo.accord.odetteftp.camel;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.MultipleConsumersSupport;
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

public class OdetteEndpoint extends ScheduledPollEndpoint implements Service, MultipleConsumersSupport {

	private static Log log = LogFactory.getLog(OdetteEndpoint.class);

	// HEADERS
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
	public static final String ODETTE_RETRY_LATER = "OdetteRetryLater";
	public static final String ODETTE_OVERRIDE = "OdetteOverride";
	public static final String ODETTE_FORCE_RESTART = "OdetteForceRestart";
	public static final String ODETTE_REASON_TEXT = "OdetteReasonText";
	public static final String ODETTE_ANSWER_REASON = "OdetteAnswerReason";
	public static final String ODETTE_NEGATIVE_RESPONSE_REASON = "OdetteNegativeEndToEndResponseReason";
	public static final String ODETTE_NERP_CREATOR = "OdetteNegativeEndToEndResponseCreator";
	public static final String ODETTE_NERP_TEXT = "OdetteNegativeEndToEndResponseCreator";
	public static final String ODETTE_FILE_DESCRIPTION = "OdetteFileDescription";
	public static final String ODETTE_TOTAL_OCTETS_SENT = "OdetteTotalOctetsSent";
	public static final String ODETTE_SEND_FILE_STARTED = "OdetteSendFileStarted";
	public static final String ODETTE_ANSWER_COUNT = "OdetteAnswerCount";

	public static final String ODETTE_WAIT_FOR_DELIVERY = "OdetteWaitForDelivery";

	private OdetteOperations operations;
	private OdetteConfiguration configuration;
	private OdetteConsumer consumer = null;
	private Set<OdetteProducer> producers = new HashSet<OdetteProducer>();

	// private Set<OdetteProducer> producers = new HashSet<OdetteProducer>();

	public OdetteEndpoint(String uri, OdetteComponent component, OdetteConfiguration configuration) throws Exception {
		super(uri, component);

		this.configuration = configuration;
		this.operations = new OdetteOperations(this);
	}

	public OdetteConfiguration getConfiguration() {
		return configuration;
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Creating OdetteConsumer");
		}

		operations.setHasInQueue();
		consumer = new OdetteConsumer(this, processor, operations);
		configureConsumer(consumer);
		return consumer;
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
		String endpointPath = getConfiguration().getWorkpath().toString() + getFileSeparator();
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

	public boolean isMultipleConsumersSupported() {
		return false;
	}

	public void notifyConsumerOf(VirtualFile incomingFile) {
		consumer.processOdetteMessage(incomingFile);
	}

	public void notifyConsumerOf(DeliveryNotification notif) {
		if (consumer == null && log.isWarnEnabled()) {
			log.warn("No consumer to process DeliveryNotification: " + notif);
		}

		if (consumer != null) {
			consumer.processOdetteMessage(notif);
		}
	}

	public IncomingFileResponse askConsumerForIncomingFile(VirtualFile incomingFile) {
		boolean routeFileRequest = configuration.isRouteFileRequest();

		IncomingFileResponse incomingFileResponse = new IncomingFileResponse(incomingFile, configuration);
		if (routeFileRequest) {
			Exchange ex = createExchange(ExchangePattern.InOut);
			configureOdetteMessage(ex.getIn(), incomingFile);
			ex.addOnCompletion(incomingFileResponse);

			try {
				consumer.getProcessor().process(ex);
			} catch (Exception e1) {
				consumer.getExceptionHandler().handleException(e1);
			}
		} else {
			incomingFileResponse.acceptFile();
		}

		return incomingFileResponse;
	}
}