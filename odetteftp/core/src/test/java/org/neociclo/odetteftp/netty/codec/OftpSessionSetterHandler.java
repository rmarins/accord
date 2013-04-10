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

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.MessageEvent;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.netty.ChannelContext;

/**
 * In/Out network stream handler that sets the Odette FTP Session object on
 * every operation. Work as upstream/downstream wrapper or in chaining handlers.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
class OftpSessionSetterHandler implements ChannelUpstreamHandler, ChannelDownstreamHandler {

    private OdetteFtpSession session;
    private ChannelUpstreamHandler upstream;
    private ChannelDownstreamHandler downstream;

    public OftpSessionSetterHandler() {
        super();
    }

    public OftpSessionSetterHandler(ChannelUpstreamHandler upstream) {
        super();
        this.upstream = upstream;
    }

    public OftpSessionSetterHandler(ChannelDownstreamHandler downstream) {
        super();
        this.downstream = downstream;
    }

    public OftpSessionSetterHandler(ChannelUpstreamHandler upstream, ChannelDownstreamHandler downstream) {
        super();
        this.upstream = upstream;
        this.downstream = downstream;
    }

    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent event) throws Exception {

        if (!(event instanceof MessageEvent)) {
            ctx.sendUpstream(event);
            return;
        }

        if (upstream == null) {
            ctx.sendUpstream(event);
        } else {
            ChannelContext.SESSION.set(event.getChannel(), session);
            upstream.handleUpstream(ctx, event);
        }
        
    }

    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent event) throws Exception {

        if (!(event instanceof MessageEvent)) {
            ctx.sendDownstream(event);
            return;
        }

        if (downstream == null) {
            ctx.sendDownstream(event);
        } else {
            ChannelContext.SESSION.set(event.getChannel(), session);
            downstream.handleDownstream(ctx, event);
        }
        
    }

    public OdetteFtpSession getSession() {
        return session;
    }

    public void setSession(OdetteFtpSession session) {
        this.session = session;
    }
}
