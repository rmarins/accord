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
package org.neociclo.odetteftp.util;

import static org.neociclo.odetteftp.EntityType.RESPONDER;
import static org.neociclo.odetteftp.EntityType.INITIATOR;

import java.nio.channels.FileChannel;

import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.oftplet.Oftplet;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class SessionHelper {

    private static final AttributeKey OFTPLET_ATTR = new AttributeKey(SessionHelper.class, "__obj_oftplet");

    private static final AttributeKey CURRENT_REQUEST_ATTR = new AttributeKey(SessionHelper.class, "__obj_currentRequest");

    private static final AttributeKey DATA_EXCHANGE_BUFFER_ATTR = new AttributeKey(SessionHelper.class, "__obj_dataExchangeBuffer");

    private static final AttributeKey FILE_CHANNEL_ATTR = new AttributeKey(SessionHelper.class, "__obj_fileChannel");

    private static final AttributeKey IS_SECURE_AUTHENTICATED_ATTR = new AttributeKey(SessionHelper.class, "__obj_isSecureAuthenticated");

    public static void setSessionOftplet(OdetteFtpSession session, Oftplet provider) {
        session.setAttribute(OFTPLET_ATTR, provider);
    }

    public static Oftplet getSessionOftplet(OdetteFtpSession session) {
        Oftplet oftplet = session.getTypedAttribute(Oftplet.class, OFTPLET_ATTR);
        return oftplet;
    }

    public static void setSessionFileChannel(OdetteFtpSession session, FileChannel fileChannel) {
        session.setAttribute(FILE_CHANNEL_ATTR, fileChannel);
    }

    public static FileChannel getSessionFileChannel(OdetteFtpSession session) {
        FileChannel fileChannel = session.getTypedAttribute(FileChannel.class, FILE_CHANNEL_ATTR);
        return fileChannel;
    }

    public static void setSessionCurrentRequest(OdetteFtpSession session, OdetteFtpObject request) {
        session.setAttribute(CURRENT_REQUEST_ATTR, request);
    }

    public static OdetteFtpObject getSessionCurrentRequest(OdetteFtpSession session) {
        OdetteFtpObject exchange = session.getTypedAttribute(OdetteFtpObject.class, CURRENT_REQUEST_ATTR);
        return exchange;
    }

    public static void setSessionOutgoingDataExchangeBuffer(OdetteFtpSession session, DataExchangeBuffer deb) {
        session.setAttribute(DATA_EXCHANGE_BUFFER_ATTR, deb);
    }

    public static DataExchangeBuffer getSessionOutgoingDataExchangeBuffer(OdetteFtpSession session) {
        return session.getTypedAttribute(DataExchangeBuffer.class, DATA_EXCHANGE_BUFFER_ATTR);
    }

    public static void setSessionSecureAuthenticated(OdetteFtpSession session) {
        session.setAttribute(IS_SECURE_AUTHENTICATED_ATTR, Boolean.TRUE);
    }

    public static boolean isSessionSecureAuthenticated(OdetteFtpSession session) {
        return session.getTypedAttribute(Boolean.class, IS_SECURE_AUTHENTICATED_ATTR, Boolean.FALSE);
    }

    public static boolean isResponder(OdetteFtpSession session) {
        return (RESPONDER == session.getEntityType());
    }

    public static boolean isInitiator(OdetteFtpSession session) {
        return (INITIATOR == session.getEntityType());
    }

    public static boolean isSendingSupported(OdetteFtpSession session) {
        return (session.getTransferMode() != TransferMode.RECEIVER_ONLY);
    }

    public static boolean isReceivingSupported(OdetteFtpSession session) {
        return (session.getTransferMode() != TransferMode.SENDER_ONLY);
    }

    private SessionHelper() {
    }
}
