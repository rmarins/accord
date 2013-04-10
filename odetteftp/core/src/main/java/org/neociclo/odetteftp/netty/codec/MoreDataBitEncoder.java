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
