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

import java.util.List;
import java.util.Queue;

import org.neociclo.accord.filetransfer.IConnectContext;
import org.neociclo.accord.filetransfer.IIncomingFileTransferRequestListener;
import org.neociclo.accord.filetransfer.spi.IOutgoingRequest;
import org.neociclo.accord.filetransfer.spi.IRetrieveRequest;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
class OftpContext {

    private OdetteFtpClientConfiguration baseConfig;
    private IConnectContext connectContext;
    private List<IIncomingFileTransferRequestListener> incomingRequestListeners;
    private Queue<IOutgoingRequest> outgoingQueue;
    private Queue<IRetrieveRequest> retrieveQueue;

    public OftpContext(IConnectContext cc, OdetteFtpClientConfiguration ocfg,
            Queue<IOutgoingRequest> outQueue, Queue<IRetrieveRequest> retQueue,
            List<IIncomingFileTransferRequestListener> inRequestListeners) {
        super();
        this.baseConfig = ocfg;
        this.connectContext = cc;
        this.outgoingQueue = outQueue;
        this.retrieveQueue = retQueue;
        this.incomingRequestListeners = inRequestListeners;
    }

    public OdetteFtpClientConfiguration getBaseConfig() {
        return baseConfig;
    }

    public IConnectContext getConnectContext() {
        return connectContext;
    }

    public List<IIncomingFileTransferRequestListener> getIncomingRequestListeners() {
        return incomingRequestListeners;
    }

    public Queue<IOutgoingRequest> getOutgoingQueue() {
        return outgoingQueue;
    }

    public Queue<IRetrieveRequest> getRetrieveQueue() {
        return retrieveQueue;
    }

}
