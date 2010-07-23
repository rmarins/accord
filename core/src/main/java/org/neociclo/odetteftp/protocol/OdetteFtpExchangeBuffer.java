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