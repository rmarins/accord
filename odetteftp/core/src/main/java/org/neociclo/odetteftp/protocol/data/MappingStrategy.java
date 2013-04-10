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
package org.neociclo.odetteftp.protocol.data;

import java.nio.channels.FileChannel;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 */
public interface MappingStrategy {

    /**
     * @param virtualFile
     * @param dataBuffer
     * @return <code>true</code> reach end of the stream (EOF).
     * @throws OdetteFtpException
     */
    boolean readData(VirtualFile virtualFile, FileChannel fileChannel, DataExchangeBuffer dataBuffer) throws OdetteFtpException;

    long writeData(VirtualFile virtualFile, DataExchangeBuffer dataBuffer, FileChannel fileChannel) throws OdetteFtpException;

}