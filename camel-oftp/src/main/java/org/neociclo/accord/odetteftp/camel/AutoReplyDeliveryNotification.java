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
