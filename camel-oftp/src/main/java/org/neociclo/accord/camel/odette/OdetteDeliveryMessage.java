package org.neociclo.accord.camel.odette;

import org.neociclo.odetteftp.protocol.DeliveryNotificationInfo;

public class OdetteDeliveryMessage extends
		OdetteMessage<DeliveryNotificationInfo> {

	private DeliveryNotificationInfo notificationInfo;

	public OdetteDeliveryMessage(DeliveryNotificationInfo dni) {
		this.notificationInfo = dni;
	}

	public DeliveryNotificationInfo getOdetteObject() {
		return notificationInfo;
	}

}
