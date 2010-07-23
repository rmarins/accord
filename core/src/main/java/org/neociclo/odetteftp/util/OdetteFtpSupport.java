/**
 * Neociclo Accord, Open Source B2Bi Middleware
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
package org.neociclo.odetteftp.util;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Date;

import org.neociclo.odetteftp.protocol.DefaultDeliveryNotification;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.NegativeResponseReason;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.protocol.DeliveryNotification.EndResponseType;
import org.neociclo.odetteftp.protocol.v20.DefaultSignedDeliveryNotification;
import org.neociclo.odetteftp.protocol.v20.EnvelopedVirtualFile;
import org.neociclo.odetteftp.protocol.v20.SignedDeliveryNotification;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OdetteFtpSupport {

    public static DeliveryNotification getReplyDeliveryNotification(VirtualFile incomingVirtualFile) {
        String creator = incomingVirtualFile.getOriginator();
        return getReplyDeliveryNotification(incomingVirtualFile, creator);
    }

    public static DeliveryNotification getReplyDeliveryNotification(VirtualFile incomingVirtualFile, String creator) {
        return getReplyDeliveryNotification(incomingVirtualFile, creator, null, null);
    }

    public static DeliveryNotification getReplyDeliveryNotification(VirtualFile incomingVirtualFile, String creator,
            NegativeResponseReason reason, String negativeReasonText) {
        if (incomingVirtualFile == null) {
            throw new NullPointerException("incomingVirtualFile");
        }
        else if (incomingVirtualFile instanceof EnvelopedVirtualFile) {
            try {
                return getReplySignedDeliveryNotification((EnvelopedVirtualFile) incomingVirtualFile, creator, reason,
                        negativeReasonText, null);
            } catch (Exception e) {
                return getReplySignedDeliveryNotification((EnvelopedVirtualFile) incomingVirtualFile, creator, reason,
                        negativeReasonText, null, null);
            }
        } else {
            return replyNormalDeliveryNotification(incomingVirtualFile, creator, reason, negativeReasonText);
        }
    }

    /**
     * Prepare the reply Signed Delivery Notification. Set automatically the
     * computed the Virtual File hash.
     * 
     * @param incomingVirtualFile
     * @param creator
     * @param reason
     * @param negativeReasonText
     * @param signature
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws IOException
     */
    public static SignedDeliveryNotification getReplySignedDeliveryNotification(EnvelopedVirtualFile incomingVirtualFile, String creator,
            NegativeResponseReason reason, String negativeReasonText, byte[] signature) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {

        byte[] virtualFileHash = null;

        if (incomingVirtualFile.getFile() != null) {
            String algorithm = EnvelopingUtil.asDigestAlgorithm(incomingVirtualFile.getCipherSuite());
            if (algorithm != null) {
                virtualFileHash = SecurityUtil.computeFileHash(incomingVirtualFile.getFile(), algorithm);
            }
        }

        return getReplySignedDeliveryNotification(incomingVirtualFile, creator, reason, negativeReasonText, virtualFileHash, signature);
    }
    
    public static SignedDeliveryNotification getReplySignedDeliveryNotification(EnvelopedVirtualFile incomingVirtualFile, String creator,
            NegativeResponseReason reason, String negativeReasonText, byte[] virtualFileHash, byte[] signature) {
        return replySignedDeliveryNotification(incomingVirtualFile, creator, reason, negativeReasonText, virtualFileHash, signature);
    }
    
    private static DeliveryNotification replyNormalDeliveryNotification(VirtualFile vf, String creator,
            NegativeResponseReason reason, String negativeReasonText) {

        EndResponseType type = (reason == null ? EndResponseType.END_TO_END_RESPONSE : EndResponseType.NEGATIVE_END_RESPONSE);

        DefaultDeliveryNotification notif = new DefaultDeliveryNotification(type);
        setNotifBasicInfo(notif, vf, creator, reason, negativeReasonText);

        return notif;
    }

    private static SignedDeliveryNotification replySignedDeliveryNotification(EnvelopedVirtualFile vf, String creator,
            NegativeResponseReason reason, String negativeReasonText, byte[] virtualFileHash, byte[] notifSignature) {

        EndResponseType type = (reason == null ? EndResponseType.END_TO_END_RESPONSE : EndResponseType.NEGATIVE_END_RESPONSE);

        DefaultSignedDeliveryNotification notif = new DefaultSignedDeliveryNotification(type);
        setNotifBasicInfo(notif, vf, creator, reason, negativeReasonText);

        notif.setVirtualFileHash(virtualFileHash);
        notif.setNotificationSignature(notifSignature);

        return null;
    }

    private static void setNotifBasicInfo(DefaultDeliveryNotification notif, VirtualFile vf, String creator,
            NegativeResponseReason reason, String negativeReasonText) {

        notif.setDatasetName(vf.getDatasetName());
        notif.setDateTime(new Date(vf.getDateTime().getTime()));
        notif.setOriginator(vf.getOriginator());
        notif.setDestination(vf.getDestination());
        notif.setUserData(vf.getUserData());

        notif.setCreator(creator);
        notif.setReason(reason);
        notif.setReasonText(negativeReasonText);
        
    }

    private OdetteFtpSupport() {
    }

}
