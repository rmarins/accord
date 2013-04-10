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
