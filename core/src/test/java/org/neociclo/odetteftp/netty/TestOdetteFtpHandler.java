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
package org.neociclo.odetteftp.netty;

import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.util.Timer;
import org.neociclo.odetteftp.EntityType;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.oftplet.ChannelCallback;
import org.neociclo.odetteftp.oftplet.OftpletFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class TestOdetteFtpHandler extends SimpleChannelHandler {

    /** Determine the ODETTE FTP entity type: Initiator or Responder. */
    private EntityType entityType;

    private OftpletFactory oftpletFactory;

    private Timer timer;

    private ChannelGroup channelGroup;

    private OdetteFtpSession session;

    
    public TestOdetteFtpHandler(EntityType entityType, OftpletFactory oftpletFactory, Timer timer, ChannelGroup channelGroup) {
        super();
        if (timer == null) {
            throw new NullPointerException("timer");
        }

        this.entityType = entityType;
        this.oftpletFactory = oftpletFactory;
        this.timer = timer;
        this.channelGroup = channelGroup;

        begin();
    }

    private void begin() {

        session = new OdetteFtpSession(entityType);

        session.setChannelCallback(new ChannelCallback() {
            public void write(Object message, Runnable execOnComplete) {
                // TODO Auto-generated method stub
                
            }
            public void closeImmediately() {
                // TODO Auto-generated method stub
                
            }
            public void close() {
                // TODO Auto-generated method stub
                
            }
        });

    }

    

}
