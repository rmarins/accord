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
package org.neociclo.odetteftp.util;

import org.neociclo.odetteftp.EntityType;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;

/**
 * @author Rafael Marins
 */
public class OdetteFtpConstants {

    /**
     * Maximum Exchange Buffer length allowed by protocol specification.
     */
    public static final int MAX_OEB_LENGTH = 99999;

    /**
     * Minimum Exchange Buffer length allowed by protocol specification.
     */
    public static final int MIN_OEB_LENGTH = 128;

    /**
     * Default ODETTE-FTP session timeout of 90 seconds (in millis).
     */
    public static final long DEFAULT_OFTP_SESSION_TIMEOUT = 90000L;

    /**
     * Default Data Exchange Buffer size of 4096 octets.
     */
    public static final int DEFAULT_OFTP_DATA_EXCHANGE_BUFFER = 4096;

    public static final int DEFAULT_OFTP_WINDOW_SIZE = 64;

    public static final int DEFAULT_RECORD_SIZE = 1024;

    /**
     * Use ODETTE-FTP v2.0 secure authentication option is disabled by default.
     */
    public static final boolean DEFAULT_OFTP_V20_SECURE_AUTH = false;

    public static final boolean DEFAULT_OFTP_RESTART = true;

    public static final boolean DEFAULT_OFTP_BUFFER_COMPRESSION = false;

    public static final boolean DEFAULT_OFTP_SPECIAL_LOGIC = false;

    public static final OdetteFtpVersion DEFAULT_OFTP_VERSION = OdetteFtpVersion.OFTP_V20;

    public static final EntityType DEFAULT_OFTP_ENTITY_TYPE = EntityType.INITIATOR;

    /**
     * Use 3DES_EDE_CBC_3KEY by default.
     */
    public static final CipherSuite DEFAULT_OFTP2_CIPHER_SUITE = CipherSuite.TRIPLEDES_RSA_SHA1;

    public static final String DEFAULT_LOGGING_LEVEL = "INFO";

    /**
     * A random number uniquely generated each time an Authentication Challenged
     * is sent. Since Odette FTP v2.0.
     */
    public static final int AUTHENTICATION_CHALLENGE_SIZE = 20;

    /**
     * Common Odette FTP port for non-secure connection is 3305.
     */
    public static final int DEFAULT_OFTP_PORT = 3305;

    /**
     * Odette FTP v2.0 secure connection port is 6619.
     */
    public static final int DEFAULT_SECURE_OFTP_PORT = 6619;

    public static final int DEFAULT_CLIENT_CONNECT_ATTEMPTS = 3;

    public static final int DEFAULT_CLIENT_CONNECT_RETRY_INTERVAL = 30;

}
