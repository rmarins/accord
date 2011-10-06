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
