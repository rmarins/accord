package org.neociclo.accord.camel.odette;

import org.neociclo.odetteftp.protocol.DeliveryNotification;

public class OdetteDeliveryMessage extends OdetteMessage<DeliveryNotification> {

	private DeliveryNotification notificationInfo;

	public OdetteDeliveryMessage(DeliveryNotification dni) {
		this.notificationInfo = dni;
	}

	public DeliveryNotification getOdetteObject() {
		return notificationInfo;
	}

}
