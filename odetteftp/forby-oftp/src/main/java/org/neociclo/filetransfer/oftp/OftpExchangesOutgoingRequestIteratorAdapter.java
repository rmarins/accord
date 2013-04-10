/**
 * The Accord Project, http://accordproject.org
 * Copyright (C) 2005-2013 Rafael Marins, http://rafaelmarins.com
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
