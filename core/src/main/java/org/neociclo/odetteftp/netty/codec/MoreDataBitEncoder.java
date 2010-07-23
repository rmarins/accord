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
package org.neociclo.odetteftp.netty.codec;

import static org.neociclo.odetteftp.netty.codec.MoreDataBitConstants.*;

import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
@Sharable
public class MoreDataBitEncoder extends LengthFieldPrepender {

    /**
     * Create encoder instance based on {@link LengthFieldPrepender}. Length
     * field contains the size of buffer data only.
     */
    public MoreDataBitEncoder() {
        super(MORE_DATA_BIT_LENGTH_FIELD_SIZE, false);
    }

}
