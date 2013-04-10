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
package org.neociclo.odetteftp.camel;

import java.net.URI;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 */
public class OftpComponent extends DefaultComponent {

	private static final Logger LOGGER = LoggerFactory.getLogger(OftpComponent.class);

    // use a shared timer for Netty (see javadoc for HashedWheelTimer)
	private volatile Timer timer;

    private OftpSettings settings;

	public OftpComponent() {
		super();
	}

	public OftpComponent(CamelContext context) {
		super(context);
	}

	@Override
	protected void doStart() throws Exception {
		// Create this component wide timer
		if (timer == null) {
            timer = new HashedWheelTimer();
			LOGGER.debug("Created the Odette FTP component timer: {}", timer);
	    	//
			// Stop timer used by Netty when shutting down
			//
	    	Runtime.getRuntime().addShutdownHook(new Thread() {
	    		@Override
	    		public synchronized void start() {
	    	    	LOGGER.debug("Shutting down Odette FTP component timer: {}", timer);
	    			timer.stop();
	    		}
	    	});
        }
		super.doStart();
	}

    @Override
    protected void doStop() throws Exception {

        super.doStop();
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {

		LOGGER.trace("Creating Odette FTP endpoint");

    	OftpSettings config;
    	if (settings != null) {
    		config = settings.clone();
    	} else {
    		config = new OftpSettings();
    	}

    	config.parseURI(new URI(uri), parameters, this);

    	IOftpEndpoint endpoint;
    	if (config.isServer()) {
    		endpoint = new OftpServerEndpoint(uri, this, config);
    	} else {
    		endpoint = new OftpClientEndpoint(uri, this, config);
    	}

    	endpoint.setTimer(getTimer());

    	LOGGER.debug("Odette FTP endpoint created: {}", endpoint);

    	return endpoint;
    }

    void setReferencesAndProperties(Object bean, Map<String, Object> parameters) throws Exception {
    	setProperties(bean, parameters);
    }

	public Timer getTimer() {
		return timer;
	}

	public void setSettings(OftpSettings settings) {
		this.settings = settings;
	}

	public OftpSettings getSettings() {
		return settings;
	}
}
