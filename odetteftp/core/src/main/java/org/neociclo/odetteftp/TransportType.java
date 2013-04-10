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
package org.neociclo.odetteftp;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public enum TransportType {

    /**
     * Internet standard transport type.
     */
    TCPIP,

    /**
     * X.25 Microtronix More-Data-Bit Gateway transport type.
     */
    X25_MBGW,

    /**
     * Integrated Services Digital Network - CAPI 2.0 enabled - transport type
     * (both native or remote CAPI are supported).
     */
    ISDN_CAPI20;

//    /**
//     * Packet Switched Networks where the Stream Transmission Buffer shouldn't
//     * be enabled. Pure packet flows used over X.25 adapter or such a switcher.
//     */
//    PSN,
//
//    /**
//     * X.25 over TCP/IP transportation based on RFC1613.
//     */
//    XOT;
}
