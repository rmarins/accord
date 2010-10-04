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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.FileConsumer;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * <p>
 * The Odette FTP Poll Consumer will connect to an Odette server and consume
 * incoming files or delivery notifications. If the Odette FTP Producer
 * associated to the same endpoint receives files or notifications to be sent,
 * they will during the same session
 * </p>
 * 
 * @author bruno
 * 
 */
public class OdetteConsumer extends ScheduledPollConsumer {

	private OdetteOperations operations;

	public OdetteConsumer(OdetteEndpoint endpoint, Processor processor, OdetteOperations operations) {
		super(endpoint, processor);

		setPollStrategy(new DefaultOdettePollingStrategy());
		setUseFixedDelay(true);

		this.operations = operations;
	}

	/**
	 * <p>
	 * Consumes data from the Odette server
	 * </p>
	 * 
	 * @param incomingVirtualFile
	 * 
	 * @param om
	 */
	public void processOdetteMessage(VirtualFile incomingVirtualFile) {
		OdetteEndpoint odetteEndpoint = (OdetteEndpoint) getEndpoint();

		OdetteConfiguration configuration = ((OdetteEndpoint) getEndpoint()).getConfiguration();
		String absolutePath = configuration.getWorkpath().getAbsolutePath();
		final GenericFile<File> file = FileConsumer.asGenericFile(absolutePath, incomingVirtualFile.getFile());

		try {
			Exchange e = odetteEndpoint.createExchange(file);
			odetteEndpoint.configureMessage(file, incomingVirtualFile, e.getIn());

			if (configuration.isDelete()) {
				e.addOnCompletion(new OdetteOnFileReceived(operations, file));
			}

			// reply with EERP (positive delivery notification) if
			// 'autoReplyDelivery'
			if (configuration.isAutoReplyDelivery()) {
				e.addOnCompletion(new AutoReplyDeliveryNotification(operations));
			}

			getProcessor().process(e);
		} catch (Exception e1) {
			getExceptionHandler().handleException(e1);
		}
	}

	@Override
	protected void poll() throws Exception {
		try {
			operations.pollServer();
		} catch (Exception e) {
			getExceptionHandler().handleException(e);
		}
	}

	public void processOdetteMessage(DeliveryNotification notif) {
		OdetteEndpoint odetteEndpoint = (OdetteEndpoint) getEndpoint();

		try {
			Exchange e = odetteEndpoint.createExchange();
			e.getIn().setBody(notif);
			odetteEndpoint.configureOdetteMessage(e.getIn(), notif);

			getProcessor().process(e);
		} catch (Exception e1) {
			getExceptionHandler().handleException(e1);
		}
	}

}
