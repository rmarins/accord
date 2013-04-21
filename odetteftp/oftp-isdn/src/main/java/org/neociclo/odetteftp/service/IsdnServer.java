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

import java.net.SocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ServerChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.util.Timer;
import org.neociclo.isdn.CapiFactory;
import org.neociclo.isdn.IsdnSocketAddress;
import org.neociclo.isdn.netty.channel.ControllerSelector;
import org.neociclo.isdn.netty.channel.IsdnConfigurator;
import org.neociclo.isdn.netty.channel.IsdnServerChannelFactory;
import org.neociclo.odetteftp.EntityType;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.util.ExecutorUtil;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 *
 */
public class IsdnServer extends Server {

	private IsdnSocketAddress bindingMsn;
	private CapiFactory capi;

	private IsdnConfigurator isdnConfigurator;
	private ControllerSelector controllerSelector;

    private Executor bossExecutor;
    private Executor workerExecutor;

    public IsdnServer(String bindingMsn, CapiFactory capi, OftpletFactory oftpletFactory) {
    	this(IsdnSocketAddress.valueOf(bindingMsn), capi, oftpletFactory);
    }

	/**
	 * @param bindingMsn
	 *            - address representation of the Multiple Subscriber Number in
	 *            which the server listening must bound.
	 * @param capi
	 *            - a Common ISDN API 2.0 compatible interface factory.
	 * @param oftpletFactory
	 */
	public IsdnServer(IsdnSocketAddress bindingMsn, CapiFactory capi, OftpletFactory oftpletFactory) {
		super(oftpletFactory);
		this.bindingMsn = bindingMsn;
		this.capi = capi;
	}

	@Override
	protected SocketAddress getAddress() {
		return getBindingMsn();
	}

	@Override
	protected ChannelPipelineFactory getPipelineFactory(OftpletFactory oftpletFactory, Timer timer,
	        ChannelGroup channelGroup) {

		IsdnPipelineFactory pipelineFactory = new IsdnPipelineFactory(EntityType.RESPONDER, oftpletFactory, timer,
		        channelGroup);

        if (isLoggingDisabled()) {
        	pipelineFactory.disableLogging();
        }

        return pipelineFactory;
	}

	@Override
	protected ServerChannelFactory createServerChannelFactory() {

    	if (bossExecutor == null) {
    		bossExecutor = Executors.newCachedThreadPool();
    		setManaged(bossExecutor);
    	}

    	if (workerExecutor == null) {
    		workerExecutor = Executors.newCachedThreadPool();
    		setManaged(workerExecutor);
    	}

		ServerChannelFactory factory = new IsdnServerChannelFactory(bossExecutor, workerExecutor, getCapi(),
		        getIsdnConfigurator(), getControllerSelector());
		return factory;
	}

    public final IsdnSocketAddress getBindingMsn() {
	    return bindingMsn;
    }


	public final CapiFactory getCapi() {
		return capi;
	}

	public IsdnConfigurator getIsdnConfigurator() {
		return isdnConfigurator;
	}

	public void setIsdnConfigurator(IsdnConfigurator isdnConfigurator) {
		this.isdnConfigurator = isdnConfigurator;
	}

	public ControllerSelector getControllerSelector() {
		return controllerSelector;
	}

	public void setControllerSelector(ControllerSelector controllerSelector) {
		this.controllerSelector = controllerSelector;
	}

	public Executor getBossExecutor() {
		return bossExecutor;
	}

	public void setBossExecutor(Executor bossExecutor) {
		this.bossExecutor = bossExecutor;
	}

	public Executor getWorkerExecutor() {
		return workerExecutor;
	}

	public void setWorkerExecutor(Executor workerExecutor) {
		this.workerExecutor = workerExecutor;
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

}
