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

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.util.ObjectHelper;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpEndpointUtil {

	public static AcceptIncomingFileHandler askConsumerForIncomingFile(IOftpEndpoint endpoint,
			OdetteFtpSession session, VirtualFile incomingFile) {

		OftpSettings endpointSettings = endpoint.getSettings();
		boolean routeCommands = endpointSettings.isRouteCommands();

		AcceptIncomingFileHandler incomingFileResponse = new AcceptIncomingFileHandler(incomingFile, endpointSettings);
		if (routeCommands) {
			Exchange e = createExchange(endpoint, ExchangePattern.InOut, incomingFile, session);
			OftpMessage m = e.getIn(OftpMessage.class);
			m.setBody(OftpCommand.acceptIncomingFileCommand(incomingFile));

			e.addOnCompletion(incomingFileResponse);
			// process the accept incoming file request exchange
			endpoint.getConsumer().process(e);
		} else {
			incomingFileResponse.acceptFile();
		}

		return incomingFileResponse;
	}

	public static Exchange createExchange(IOftpEndpoint endpoint, OdetteFtpObject obj) {
		return createExchange(endpoint, obj, null);
	}

	public static Exchange createExchange(IOftpEndpoint endpoint, OdetteFtpObject obj, OdetteFtpSession session) {
		return createExchange(endpoint, endpoint.getExchangePattern(), obj, session);
	}

	public static Exchange createExchange(IOftpEndpoint endpoint, ExchangePattern pattern, OdetteFtpObject obj,
			OdetteFtpSession session) {

		Exchange e = new DefaultExchange(endpoint, pattern);
		e.setProperty(Exchange.BINDING, endpoint.getBinding());
		e.setIn(new OftpMessage(obj, session));

		return e;
	}

	public static void notifyConsumerOf(IOftpEndpoint endpoint, VirtualFile receivedFile) {
		OftpSettings settings = endpoint.getSettings();

		Exchange exchange = createExchange(endpoint, receivedFile);

		if (settings.isDelete()) {
			exchange.addOnCompletion(new DeleteReceivedFile(endpoint, receivedFile));
		}

		endpoint.getConsumer().process(exchange);
	}

	public static void notifyConsumerOf(IOftpEndpoint endpoint, OftpEvent event) {

		OftpSettings endpointSettings = endpoint.getSettings();
		if (!endpointSettings.isRouteEvents()) {
			return;
		}

		ObjectHelper.notNull(event, "event");

		Exchange exchange = createExchange(endpoint, ExchangePattern.InOnly, event.getRequestObject(),
				event.getSession());
		OftpMessage message = (OftpMessage) exchange.getIn();
		message.setBody(event);

		// dispatch the event exchange
		endpoint.getConsumer().process(exchange);


	}

	public static List<OdetteFtpObject> askConsumerForUserOutgoingExchanges(IOftpEndpoint endpoint, String userCode,
			OdetteFtpSession session) {

		OftpSettings endpointSettings = endpoint.getSettings();
		boolean routeCommands = endpointSettings.isRouteCommands();

		if (!routeCommands) {
			return null;
		}

		final List<OdetteFtpObject> result = new ArrayList<OdetteFtpObject>();

		Exchange e = createExchange(endpoint, ExchangePattern.InOut, null, session);
		OftpMessage m = e.getIn(OftpMessage.class);
		m.setBody(OftpCommand.retrieveUserOutgoingExchangesCommand());

		e.addOnCompletion(new Synchronization() {
			@SuppressWarnings("unchecked")
			public void onComplete(Exchange exchange) {

				// deal with grouped exchanges if any
				List<Exchange> grouped = exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class);
				if (grouped != null) {
					for (Exchange g : grouped) {
						try {
							addOutgoingExchangeToList(g, result);
						} catch (InvalidPayloadException ipe) {
							g.setException(ipe);
						}
					}
				}

				// extract from a single exchange returned
				try {
					addOutgoingExchangeToList(exchange, result);
				} catch (InvalidPayloadException ipe) {
					exchange.setException(ipe);
					
				}
			}
			public void onFailure(Exchange exchange) {
				// do nothing
			}
		});

		endpoint.getConsumer().process(e);

		return result;
	}

	private static void addOutgoingExchangeToList(Exchange e, List<OdetteFtpObject> result) throws InvalidPayloadException {
		Message out = e.getOut();
		if (out != null) {
			OdetteFtpObject o = out.getMandatoryBody(OdetteFtpObject.class);
			if (o != null) {
				result.add(o);
			}
		}
	}

	private OftpEndpointUtil() {
	}

}
