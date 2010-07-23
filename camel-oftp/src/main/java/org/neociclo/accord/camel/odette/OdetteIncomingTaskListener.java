package org.neociclo.accord.camel.odette;

import org.neociclo.odetteftp.client.OftpFile;
import org.neociclo.odetteftp.protocol.DeliveryNotificationInfo;

public interface OdetteIncomingTaskListener {

	void incoming(OftpFile file);

	void incoming(DeliveryNotificationInfo notInfo);

}
