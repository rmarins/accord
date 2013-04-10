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

import static org.neociclo.odetteftp.util.CommandFormatConstants.*;

import static org.neociclo.odetteftp.protocol.CommandIdentifier.CD;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.CDT;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.RTR;
import static org.neociclo.odetteftp.protocol.CommandIdentifier.SSRM;
import static org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13.CDT_V13;
import static org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13.CD_V13;
import static org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13.RTR_V13;
import static org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13.SSRM_V13;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public abstract class CommandBuilder {

    /**
     * Default charset defined in Odette FTP protocol specification.
     */
    public static final String DEFAULT_PROTOCOL_CHARSET = "ISO_646.IRV:1991";

    protected static final String ZERO = "0";

    /**
     * Create the Change Direction command.
     * 
     * @return The Change Direction command with the corresponding values.
     */
    public static CommandExchangeBuffer changeDirection() {
        CommandExchangeBuffer cd = new CommandExchangeBuffer(CD_V13);
        cd.setAttribute(CDCMD_FIELD, String.valueOf(CD.getCode()));
        return cd;
    }

    /**
     * Start Session Ready Message.
     * 
     * @return Corresponding <code>CommandExchangeBuffer</code>.
     */
    public static CommandExchangeBuffer readyMessage() {
        CommandExchangeBuffer ssrm = new CommandExchangeBuffer(SSRM_V13);

        ssrm.setAttribute(SSRMCMD_FIELD, String.valueOf(SSRM.getCode()));
        ssrm.setAttribute(SSRMMSG_FIELD, SSRMMSG_VALUE);
        ssrm.setAttribute(SSRMCR_FIELD, PROTOCOL_CARRIAGE_RETURN);

        return ssrm;
    }

    /**
     * Create the Ready to Receive command.
     * 
     * @return The Ready to Receive command with the corresponding values.
     */
    public static CommandExchangeBuffer readyToReceive() {
        CommandExchangeBuffer rtr = new CommandExchangeBuffer(RTR_V13);
        rtr.setAttribute(RTRCMD_FIELD, String.valueOf(RTR.getCode()));
        return rtr;
    }

    /**
     * Create the Set Credit command which is used to avoid congestion at the
     * protocol level a flow control.
     * 
     * @return The Set Credit command with the corresponding values.
     */
    public static CommandExchangeBuffer setCredit() {

        CommandExchangeBuffer cdt = new CommandExchangeBuffer(CDT_V13);
        cdt.setAttribute(CDTCMD_FIELD, String.valueOf(CDT.getCode()));

        return cdt;
    }

    protected static String yesNo(boolean set) {
        return (set ? "Y" : "N");
    }

    protected static boolean isEmpty(String text) {
        return (text == null || "".equals(text));
    }
}