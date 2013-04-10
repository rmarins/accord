/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2010 Neociclo, http://www.neociclo.com
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
package org.neociclo.filetransfer.oftp;

import java.util.Queue;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;

import org.neociclo.accord.filetransfer.IConnectContext;
import org.neociclo.accord.filetransfer.spi.IOutgoingRequest;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.support.OftpletAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class EventBasedOftplet extends OftpletAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventBasedOftplet.class);

    private final OdetteFtpClientConfiguration baseConfig;
    private final IConnectContext connectContext;

    // containers' instance queues (referenced)
    private final Queue<IOutgoingRequest> outgoingQueue;

    private String userOid;
    private OdetteFtpSession session;

    private EventBasedOftpletListener oftpletListener;

    public EventBasedOftplet(OftpContext oftpContext) {
        super();
        this.connectContext = oftpContext.getConnectContext();
        this.baseConfig = oftpContext.getBaseConfig();
        this.oftpletListener = new EventBasedOftpletListener(oftpContext.getRetrieveQueue(), oftpContext.getIncomingRequestListeners());
//        this.incomingRequestListeners = oftpContext.getIncomingRequestListeners();
        this.outgoingQueue = oftpContext.getOutgoingQueue();
//        this.retrieveQueue = oftpContext.getRetrieveQueue();
    }

    // -------------------------------------------------------------------------
    // Oftplet implementation
    // -------------------------------------------------------------------------

    @Override
    public void init(OdetteFtpSession session) {
        this.session = session;
        setupSession();
    }

    private void setupSession() {

        session.setDataBufferSize(baseConfig.getDebSize());
        session.setWindowSize(baseConfig.getWindowSize());
        session.setCompressionSupport(baseConfig.isCompressionSupported());
        session.setTimeout(baseConfig.getTimeout());
        session.setSecureAuthentication(baseConfig.useSecureAuthentication());

        session.setTransferMode(baseConfig.getMode());

        // method for setting odette-ftp initiator's authentication
        this.userOid = baseConfig.getUserOid();
        session.setUserCode(userOid);
        session.setUserPassword(callbackRetrieveUserPassword());

        if (session.getUserPassword() == null) {
            LOGGER.warn("OID({}) Connecting using no user password.", userOid);
        }

    }

//    @Override
//    public Iterator<OdetteFtpExchange> getTransmitExchanges() {
//
//        if (transmitExchangesIterator == null && outgoingQueue != null) {
//            transmitExchangesIterator = new OftpExchangesOutgoingRequestIteratorAdapter(outgoingQueue.iterator());
//        }
//
//        return transmitExchangesIterator;
//
//    }

    // -------------------------------------------------------------------------
    // Class specific implementation
    // -------------------------------------------------------------------------

    private String callbackRetrieveUserPassword() {

        String password = null;
        CallbackHandler callbackHandler = null;

        if (connectContext == null) {
            LOGGER.warn("OID({}) Cannot determine user password. No IConnectContext were provided.", userOid);
        } else {
            callbackHandler = connectContext.getCallbackHandler();
        }

        if (callbackHandler == null) {
            LOGGER.warn("OID({}) Cannot determine user password. No CallbackHandler were provided.", userOid);
        } else {
            PasswordCallback retrievePasswordCallback = new PasswordCallback(userOid, false);
            try {
                callbackHandler.handle(new Callback[] { retrievePasswordCallback });
            } catch (Throwable t) {
                LOGGER.error("OID({}) Cannot determine user password. Callback error: {}", userOid, t.getMessage());
            }
            password = String.valueOf(retrievePasswordCallback.getPassword());
        }

        return password;
    }

}
