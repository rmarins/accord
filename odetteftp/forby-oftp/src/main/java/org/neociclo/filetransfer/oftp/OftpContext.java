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
