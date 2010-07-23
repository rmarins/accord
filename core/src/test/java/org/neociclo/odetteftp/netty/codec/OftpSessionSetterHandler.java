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
package org.neociclo.odetteftp.netty.codec;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.MessageEvent;
import org.neociclo.odetteftp.OdetteFtpSession;

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
            ctx.setAttachment(session);
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
            ctx.setAttachment(session);
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
