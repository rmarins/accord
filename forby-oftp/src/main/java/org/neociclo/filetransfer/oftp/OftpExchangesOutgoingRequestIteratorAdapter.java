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

import java.io.File;
import java.util.Iterator;

import org.neociclo.accord.filetransfer.IFileTransferInfo;
import org.neociclo.accord.filetransfer.spi.IOutgoingRequest;
import org.neociclo.accord.filetransfer.spi.ISendDeliveryAckOutgoingRequest;
import org.neociclo.accord.filetransfer.spi.ISendFileOutgoingRequest;
import org.neociclo.odetteftp.protocol.OdetteFtpExchange;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
class OftpExchangesOutgoingRequestIteratorAdapter implements Iterator<OdetteFtpExchange> {

    private Iterator<IOutgoingRequest> innerIterator;

    public OftpExchangesOutgoingRequestIteratorAdapter(Iterator<IOutgoingRequest> innerIt) {
        this.innerIterator = innerIt;
    }

    public boolean hasNext() {
        return innerIterator.hasNext();
    }

    public OdetteFtpExchange next() {

        IOutgoingRequest req = innerIterator.next();
        if (req instanceof ISendFileOutgoingRequest) {
            return requestToOftpExchange((ISendFileOutgoingRequest) req);
        } else if (req instanceof ISendDeliveryAckOutgoingRequest) {
            return requestToDeliveryNotif((ISendDeliveryAckOutgoingRequest) req);
        } else {
            
        }
    }

    public void remove() {
        innerIterator.remove();
    }

    private OdetteFtpExchange requestToOftpExchange(ISendFileOutgoingRequest req) {

        IFileTransferInfo fti = req.getFileTransferInfo();
        File sourceFile = fti.getFile();

        

        return null;
    }

    private OdetteFtpExchange requestToDeliveryNotif(ISendDeliveryAckOutgoingRequest req) {
        // TODO Auto-generated method stub
        return null;
    }

}
