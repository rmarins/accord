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
//      sp.addLast("OdetteFtp-HANDLER", new OdetteFtpChannelHandler(EntityType.RESPONDER, createServerOftpletFactory(), timer, null));

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
