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
package org.neociclo.odetteftp.protocol;

import java.nio.ByteBuffer;

import org.neociclo.odetteftp.OdetteFtpException;

/**
 * The communication between ODETTE-FTP entities is basically done by sending
 * and receiving messages in Exchange Buffers via the transport layer. An
 * ODETTE-FTP Exchange Buffer can have either a Command Exchange Buffer or Data
 * Exchange Buffer.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public interface OdetteFtpExchangeBuffer {

    public ByteBuffer getRawBuffer();

    /**
     * Return the corresponding <code>CommandIdentifier</code> relative to the
     * Exchange Buffer contents. Its value is parsed from the first octet of the
     * Exchange Buffer specified through class constructor.
     * <p>
     * The CommandIdentifier defines the format of the Exchange Buffer contents.
     * 
     * @return CommandIdentifier Instance relative to the command type of this
     *         Exchange Buffer.
     * @throws CommandNotRecognisedException
     *         Command identifier not recognized in the specified buffer.
     * @see org.neociclo.odetteftp.service.CommandIdentifier
     */
    public CommandIdentifier getIdentifier() throws OdetteFtpException;

    public int getSize();

}