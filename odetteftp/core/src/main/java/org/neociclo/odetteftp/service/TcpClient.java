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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.util.Timer;
import org.neociclo.odetteftp.EntityType;
import org.neociclo.odetteftp.TransportType;
import org.neociclo.odetteftp.netty.OdetteFtpPipelineFactory;
import org.neociclo.odetteftp.netty.SslHandlerFactory;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.util.ExecutorUtil;

/**
 * @author Rafael Marins
 */
public class TcpClient extends Client {

    private static final int DEFAULT_NON_SSL_PORT = 3305;
	private static final int DEFAULT_SSL_PORT = 6619;

	private Executor bossExecutor;
	private Executor workerExecutor;

    private SSLContext sslContext;

	public TcpClient() {
		this(null);
	}

    public TcpClient(SSLContext sslContext) {
    	super();
    	this.sslContext = sslContext;
    }

    public synchronized void connect(String host, boolean await) throws Exception {
    	connect(host, -1, await);
    }

    public synchronized void connect(String host, int port, boolean await) throws Exception {
    	InetSocketAddress remoteAddress;
    	
    	if (port > 0) {
    		remoteAddress = new InetSocketAddress(host, port);
    	} else {
    		if (sslContext == null) {
	    		remoteAddress = new InetSocketAddress(host, DEFAULT_NON_SSL_PORT);
	    	} else {
	    		remoteAddress = new InetSocketAddress(host, DEFAULT_SSL_PORT);
	    	}
    	}

    	connect(remoteAddress, await);
    }

	@Override
    public ChannelFactory getChannelFactory() {

		// creates one channel-factory per object instance
        if (super.getChannelFactory() == null) {

        	if (bossExecutor == null) {
                bossExecutor = Executors.newCachedThreadPool();
                setManaged(bossExecutor);
            }

            if (workerExecutor == null) {
            	workerExecutor = Executors.newCachedThreadPool();
                setManaged(workerExecutor);
            }

        	setChannelFactory(new NioClientSocketChannelFactory(bossExecutor, workerExecutor));
        }

        return super.getChannelFactory();
    }

    @Override
    protected ChannelPipelineFactory getPipelineFactory(OftpletFactory oftpletFactory, Timer timer) {

        SslHandlerFactory sslHandlerFactory = new SslHandlerFactory() {
			public SslHandler createSslHandler() {
				SslHandler sslHandler = null;
		        if (sslContext != null) {
		        	SSLEngine engine = sslContext.createSSLEngine();
		    		engine.setUseClientMode(true);
		    		engine.setEnableSessionCreation(true);
		    		
		    		
		    		//MPA - contrib. from Mathieu Pasture
					String[] protocols = sslContext.getSupportedSSLParameters().getProtocols();
	                List<String> newProtocolList = new ArrayList<String>();
	                for (String protocol : protocols) {
	                    if( !protocol.equalsIgnoreCase("SSLv2Hello")){
	                        newProtocolList.add( protocol );
	                    }
	                }
	                String[] newProtocolArray = newProtocolList.toArray(new String[newProtocolList.size()]); 
	                engine.setEnabledProtocols(newProtocolArray);
	                
		    		
		    		
		            sslHandler = new SslHandler(engine);
		        }
				return sslHandler;
			}
		};

        OdetteFtpPipelineFactory pipelineFactory = new OdetteFtpPipelineFactory(EntityType.INITIATOR, oftpletFactory,
                timer, getTransportType(), sslHandlerFactory, null);

        if (isLoggingDisabled()) {
        	pipelineFactory.disableLogging();
        }

        return pipelineFactory;
    }

    public Executor getBossExecutor() {
        return bossExecutor;
    }

	/**
	 * The Executor which was specified should be terminated manually by calling
	 * {@link ExecutorUtil#terminate(Executor...)} when your application shuts
	 * down.
	 * 
	 * @param executor
	 */
	public void setWorkerExecutor(Executor workerExecutor) {
        if (getChannel() != null) {
            throw new IllegalStateException("Channel already created. Executor must be set before connect.");
        }

		this.workerExecutor = workerExecutor;
	}

	public Executor getWorkerExecutor() {
		return workerExecutor;
	}

	/**
	 * The Executor which was specified should be terminated manually by calling
	 * {@link ExecutorUtil#terminate(Executor...)} when your application shuts
	 * down.
	 * 
	 * @param executor
	 */
    public void setBossExecutor(Executor executor) {
        if (getChannel() != null) {
            throw new IllegalStateException("Channel already created. Executor must be set before connect.");
        }

        this.bossExecutor = executor;
    }

    @Override
    protected void releaseExternalResources() {
    	if (isManaged(bossExecutor)) {
    		ExecutorUtil.terminate(bossExecutor);
    	}
    	if (isManaged(workerExecutor)) {
    		ExecutorUtil.terminate(workerExecutor);
    	}
    	super.releaseExternalResources();
    }

	protected TransportType getTransportType() {
		return TransportType.TCPIP;
	}
}
