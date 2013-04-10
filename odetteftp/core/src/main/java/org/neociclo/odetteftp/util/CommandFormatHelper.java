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
package org.neociclo.odetteftp.util;

import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V12;
import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V13;
import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V14;
import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V20;

import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.protocol.CommandFormat;
import org.neociclo.odetteftp.protocol.CommandIdentifier;
import org.neociclo.odetteftp.protocol.v13.ReleaseFormatVer13;
import org.neociclo.odetteftp.protocol.v14.ReleaseFormatVer14;
import org.neociclo.odetteftp.protocol.v20.ReleaseFormatVer20;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class CommandFormatHelper {

    public static CommandFormat resolveByVersion(OdetteFtpVersion version, CommandIdentifier identifier) {

        if (identifier == CommandIdentifier.DATA)
            return null;

        CommandFormat format = null;

        if ((version == OFTP_V12) || (version == OFTP_V13))
            format = ReleaseFormatVer13.getFormat(identifier);

        else if (version == OFTP_V14)
            format = ReleaseFormatVer14.getFormat(identifier);

        else if (version == OFTP_V20)
            format = ReleaseFormatVer20.getFormat(identifier);

        return format;
    }

}
