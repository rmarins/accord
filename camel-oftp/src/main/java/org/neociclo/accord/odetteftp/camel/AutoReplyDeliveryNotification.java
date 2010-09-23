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

import static org.neociclo.accord.odetteftp.camel.OdetteEndpoint.ODETTE_NEGATIVE_RESPONSE_REASON;
import static org.neociclo.accord.odetteftp.camel.OdetteEndpoint.ODETTE_NERP_CREATOR;
import static org.neociclo.accord.odetteftp.camel.OdetteEndpoint.ODETTE_NERP_TEXT;
import static org.neociclo.accord.odetteftp.camel.OdetteEndpoint.ODETTE_VIRTUAL_FILE;
import static org.neociclo.odetteftp.util.OdetteFtpSupport.getReplyDeliveryNotification;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.spi.Synchronization;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.NegativeResponseReason;
import org.neociclo.odetteftp.protocol.VirtualFile;

public final class AutoReplyDeliveryNotification implements Synchronization {

	private final OdetteOperations operations;

	AutoReplyDeliveryNotification(OdetteOperations operations) {
		this.operations = operations;
	}

	public void onComplete(Exchange exchange) {
		VirtualFile virtualFile = exchange.getIn().getHeader(ODETTE_VIRTUAL_FILE, VirtualFile.class);
		DeliveryNotification notif = getReplyDeliveryNotification(virtualFile);
		operations.offer(notif);
	}

	public void onFailure(Exchange exchange) {
		Message in = exchange.getIn();
		VirtualFile virtualFile = in.getHeader(ODETTE_VIRTUAL_FILE, VirtualFile.class);
		Message out = exchange.getOut();
		NegativeResponseReason negReason = out.getHeader(ODETTE_NEGATIVE_RESPONSE_REASON, NegativeResponseReason.class);
		String creatorNERP = out.getHeader(ODETTE_NERP_CREATOR, String.class);
		String textNERP = out.getHeader(ODETTE_NERP_TEXT, String.class);
		DeliveryNotification notif = getReplyDeliveryNotification(virtualFile, creatorNERP, negReason, textNERP);
		operations.offer(notif);
	}

}
