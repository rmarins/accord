/**
 * Neociclo Accord, Open Source B2B Integration Suite
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
 * @version $Rev$ $Date$
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
        }
		super.doStart();
	}

    @Override
    protected void doStop() throws Exception {

    	//
		// Shutdown timer used by Netty
		//
    	Runtime.getRuntime().addShutdownHook(new Thread() {
    		@Override
    		public synchronized void start() {
    	    	LOGGER.debug("Shutting down Odette FTP component timer: {}", timer);
    			timer.stop();
    		}
    	});

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
