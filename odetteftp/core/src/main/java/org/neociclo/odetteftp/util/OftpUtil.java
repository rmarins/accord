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
package org.neociclo.odetteftp.util;

import static org.neociclo.odetteftp.util.OdetteFtpConstants.*;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpUtil {

    public static boolean isEmpty(String value) {
        return (value == null || "".equals(value.trim()));
    }

    public static byte[] generateRandomChallenge(int size) {

        byte[] challenge = new byte[size];

        SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        random.nextBytes(challenge);
        return challenge;
    }

    public static long getFileSize(File file) {
        return getFileSize(file.length());
    }

    public static long getFileSize(long octets) {

        long blocks;
    
        blocks = (octets / DEFAULT_RECORD_SIZE);
        if ((octets % DEFAULT_RECORD_SIZE) > 0)
            blocks++;
    
        return blocks;
    }

    public static String toHexString(Integer num) {
        String hex = Integer.toHexString(num.intValue());
        switch (hex.length()) {
        case 0:
            hex = "00000000";
            break;
        case 1:
            hex = "0000000" + hex;
            break;
        case 2:
            hex = "000000" + hex;
            break;
        case 3:
            hex = "00000" + hex;
            break;
        case 4:
            hex = "0000" + hex;
            break;
        case 5:
            hex = "000" + hex;
            break;
        case 6:
            hex = "00" + hex;
            break;
        case 7:
            hex = "0" + hex;
            break;
        }
        return hex;
    }

}
