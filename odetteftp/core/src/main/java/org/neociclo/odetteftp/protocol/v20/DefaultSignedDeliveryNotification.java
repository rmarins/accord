/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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
