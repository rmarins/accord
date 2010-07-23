/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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
package org.neociclo.odetteftp.protocol.v20;

import java.util.Arrays;

import org.neociclo.odetteftp.protocol.DefaultDeliveryNotification;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class DefaultSignedDeliveryNotification extends DefaultDeliveryNotification implements SignedDeliveryNotification {

    private static final long serialVersionUID = 1L;

    private byte[] virtualFileHash;

    private byte[] notificationSignature;

    public DefaultSignedDeliveryNotification(EndResponseType type) {
        super(type);
    }

    public byte[] getVirtualFileHash() {
        return virtualFileHash;
    }

    public void setVirtualFileHash(byte[] virtualFileHash) {
        this.virtualFileHash = virtualFileHash;
    }

    public byte[] getNotificationSignature() {
        return notificationSignature;
    }

    public void setNotificationSignature(byte[] endResponseSignature) {
        this.notificationSignature = endResponseSignature;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(notificationSignature);
        result = prime * result + Arrays.hashCode(virtualFileHash);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof DefaultSignedDeliveryNotification))
            return false;
        DefaultSignedDeliveryNotification other = (DefaultSignedDeliveryNotification) obj;
        if (!Arrays.equals(notificationSignature, other.notificationSignature))
            return false;
        if (!Arrays.equals(virtualFileHash, other.virtualFileHash))
            return false;
        return true;
    }

}
