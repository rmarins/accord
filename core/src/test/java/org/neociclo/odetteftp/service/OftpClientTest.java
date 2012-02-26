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
package org.neociclo.odetteftp.service;

import java.util.concurrent.Executor;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ServerChannel;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neociclo.odetteftp.netty.codec.OdetteFtpDecoder;
import org.neociclo.odetteftp.netty.codec.OdetteFtpEncoder;
import org.neociclo.odetteftp.netty.codec.StbDecoder;
import org.neociclo.odetteftp.netty.codec.StbEncoder;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpClientTest {

    private static Executor executor;
    private static Timer timer;

    @BeforeClass
    public static void init() {
        timer = new HashedWheelTimer();
    }

    @AfterClass
    public static void destroy() {
        timer.stop();
    }

    /** Server Bootstrap. */
    protected ServerBootstrap sb;
    /** Server Channel. */
    protected ServerChannel sc;

    /** Client Bootstrap. */
    protected ClientBootstrap cb;
    /** Client Channel. */
    protected Channel cc;

    /** Odette FTP client (for TCP/IP). */
    protected TcpClient client;

    @Before
    public void setUp() {

        // Server setup
        sb = new ServerBootstrap(new DefaultLocalServerChannelFactory());

        LocalAddress portOne = new LocalAddress(1);

        ChannelPipeline sp = sb.getPipeline();
        sp.addLast("stb-decoder", new StbDecoder());
        sp.addLast("stb-encoder", new StbEncoder());
        sp.addLast("oftp-decoder", new OdetteFtpDecoder());
        sp.addLast("oftp-encoder", new OdetteFtpEncoder());
//        sp.addLast("OdetteFtp-HANDLER", new OdetteFtpChannelHandler(EntityType.RESPONDER, createServerOftpletFactory(), timer, null));

        sc = (ServerChannel) sb.bind(portOne);

        cb = new ClientBootstrap(new DefaultLocalClientChannelFactory());

        ChannelPipeline cp = cb.getPipeline();
        cp.addLast("stb-decoder", new StbDecoder());
        cp.addLast("stb-encoder", new StbEncoder());
        cp.addLast("oftp-decoder", new OdetteFtpDecoder());
        cp.addLast("oftp-encoder", new OdetteFtpEncoder());
//        cp.addLast("OdetteFtp-HANDLER", new OdetteFtpChannelHandler(EntityType.RESPONDER, createServerOftpletFactory(), timer, null));

    }

    @After
    public void tearDown() {
        sc.close();
    }

    @Test
    public void testDoNothing() {
    	
    }

}
