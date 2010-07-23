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
package org.neociclo.filetransfer.oftp;

import static org.neociclo.filetransfer.oftp.util.OftpConstants.*;

import java.util.Date;
import java.util.HashMap;

import org.neociclo.accord.filetransfer.DefaultDeliveryAckInfo;
import org.neociclo.accord.filetransfer.IDeliveryAckInfo;
import org.neociclo.odetteftp.protocol.IDeliveryNotification;
import org.neociclo.odetteftp.protocol.NegativeResponseReason;
import org.neociclo.odetteftp.protocol.v20.ISignedDeliveryNotification;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpDeliveryAckInfo extends DefaultDeliveryAckInfo implements ISignedDeliveryNotification {

    private static final long serialVersionUID = 1L;

    public OftpDeliveryAckInfo(IDeliveryAckInfo deliveryAckInfo) {
        super(new HashMap<String, Object>(deliveryAckInfo.getProperties()));
    }

    public OftpDeliveryAckInfo(IDeliveryNotification notif) {
        super(new HashMap<String, Object>());

        if (notif == null) {
            throw new NullPointerException("notif");
        }

        setDatasetName(notif.getDatasetName());
        setDateTime(notif.getDateTime());
        setOriginator(notif.getOriginator());
        setDestination(notif.getDestination());
        setUserData(notif.getUserData());

        setType(notif.getType());
        setReason(notif.getReason());
        setReasonText(notif.getReasonText());

        if (notif instanceof ISignedDeliveryNotification) {
            ISignedDeliveryNotification signedNotif = (ISignedDeliveryNotification) notif;
            setNotificationSignature(signedNotif.getNotificationSignature());
            setVirtualFileHash(signedNotif.getVirtualFileHash());
        }

    }

    public byte[] getNotificationSignature() {
        return (byte[]) properties.get(NOTIFICATION_SIGNATURE_PROP);
    }

    public void setNotificationSignature(byte[] signature) {
        properties.put(NOTIFICATION_SIGNATURE_PROP, signature);
    }

    public byte[] getVirtualFileHash() {
        return (byte[]) properties.get(NOTIFICATION_FILE_HASH_PROP);
    }

    public void setVirtualFileHash(byte[] hash) {
        properties.put(NOTIFICATION_FILE_HASH_PROP, hash);
    }

    public String getCreator() {
        return (String) properties.get(NOTIFICATION_CREATOR_PROP);
    }

    public void setCreator(String creator) {
        properties.put(NOTIFICATION_CREATOR_PROP, creator);
    }

    public NegativeResponseReason getReason() {
        return (NegativeResponseReason) properties.get(NOTIFICATION_NEGATIVE_REASON_PROP);
    }

    public void setReason(NegativeResponseReason reason) {
        properties.put(NOTIFICATION_NEGATIVE_REASON_PROP, reason);
    }

    public String getReasonText() {
        return (String) properties.get(NOTIFICATION_REASON_TEXT_PROP);
    }

    public void setReasonText(String text) {
        properties.put(NOTIFICATION_REASON_TEXT_PROP, text);
    }

    public EndResponseType getType() {
        return (EndResponseType) properties.get(NOTIFICATION_TYPE_PROP);
    }

    public void setType(EndResponseType type) {
        properties.put(NOTIFICATION_TYPE_PROP, type);
    }

    public String getDatasetName() {
        return (String) properties.get(DATASET_NAME_PROP);
    }

    public void setDatasetName(String dsn) {
        properties.put(DATASET_NAME_PROP, dsn);
    }

    public Date getDateTime() {
        return (Date) properties.get(DATE_TIME_PROP);
    }

    public void setDateTime(Date dateTime) {
        properties.put(DATE_TIME_PROP, dateTime);
    }

    public String getDestination() {
        return (String) properties.get(DESTINATION_PROP);
    }

    public void setDestination(String oid) {
        properties.put(DESTINATION_PROP, oid);
    }

    public String getOriginator() {
        return (String) properties.get(ORIGINATOR_PROP);
    }

    public void setOriginator(String oid) {
        properties.put(ORIGINATOR_PROP, oid);
    }

    public String getUserData() {
        return (String) properties.get(USER_DATA_PROP);
    }

    public void setUserData(String data) {
        properties.put(USER_DATA_PROP, data);
    }

}
