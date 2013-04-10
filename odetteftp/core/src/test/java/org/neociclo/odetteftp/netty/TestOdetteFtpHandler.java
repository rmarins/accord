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
