package org.neociclo.accord.odetteftp.camel;

import org.apache.camel.impl.DefaultMessage;
import org.neociclo.odetteftp.protocol.DeliveryNotification;

public class OdetteDeliveryMessage extends DefaultMessage {

	private DeliveryNotification notificationInfo;

	public OdetteDeliveryMessage(DeliveryNotification dni) {
		this.notificationInfo = dni;
	}

	public DeliveryNotification getOdetteObject() {
		return notificationInfo;
	}

}
