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
package org.neociclo.odetteftp.oftplet;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.security.SecurityContext;

/**
 * Define the methods that <i>Oftplets</i> must implement.
 * <p/>
 * An <i>Oftplet</i> is a small Java program that run within the Odette FTP
 * Library. Oftplets provides complementary implementation to the
 * {@link OdetteFtpHandler} on the support for protocol data movement and
 * acknowledgment.
 * <p/>
 * <ol>
 * <li>The <i>Oftplet</i> is constructed using an {@link OftpletFactory} on the
 * event of the channel open.</li>
 * <li>Then initialized with the {@link Oftplet#init(OdetteFtpSession)}.</li>
 * <li>Callback methods are invoked as appropriate in circumstance of performing
 * protocol procedures.</li>
 * <li>The <i>Oftplet</i> is taken out of service, then destroyed with the
 * {@link Oftplet#destroy()} method.</li>
 * </ol>
 * <p/>
 * The referred <i>Oftplet container</i> will always be the
 * {@link OdetteFtpHandler} during the life cycle of the communication session.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public interface Oftplet {

    /**
     * Called by the <i>Oftplet</i> container to indicate that it is placed into
     * service.
     * <p/>
     * The implementation of this method might set up the given session in the
     * context with parameters configuration for the initial handshaking (SSID).
     * 
     * @param s
     */
    void init(OdetteFtpSession session) throws OdetteFtpException;

    /**
     * Called by the <i>Oftplet</i> container to indicate it is being taken out
     * of service.
     */
    void destroy();

    void onSessionStart();

    /**
     * Callback method indicating protocol release of End Session (ESID) command
     * - normal or abnormal protocol termination.
     * <p/>
     * There ISN'T a corresponding <i>onSessionStart()</i> method but the
     * {@link Oftplet#init(OdetteFtpContext)} method can be used with similar
     * way, since it's invoked when the protocol session is just created.
     * @param reasonText
     */
    void onSessionEnd();

    /**
     * Callback method called on event of an unexpected exception during the
     * communication session and while performing file transfer. The
     * <i>Oftplet</i> might check the current ODETTE-FTP entity state and if in
     * the circumstance of file transfer when handling the exceptions thrown.
     * 
     * @param cause
     */
    void onExceptionCaught(Throwable cause);

    /**
     * @param version
     * @return
     */
    boolean isProtocolVersionSupported(OdetteFtpVersion version);

    SecurityContext getSecurityContext();
    
    OftpletSpeaker getSpeaker();

    OftpletListener getListener();

}
