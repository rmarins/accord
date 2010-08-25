package org.neociclo.accord.camel.odette;

import java.net.URI;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

public class OdetteComponent extends DefaultComponent {

	private OdetteConfiguration configuration = new OdetteConfiguration();

	public OdetteConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(OdetteConfiguration defaultConfiguration) {
		this.configuration = defaultConfiguration;
	}

	@Override
	protected Endpoint createEndpoint(String uri, String remaining,
			Map<String, Object> parameters) throws Exception {

		OdetteConfiguration resultConfig = configuration.clone();
		resultConfig.configure(new URI(uri));
		setProperties(resultConfig, parameters);

		OdetteEndpoint endpoint = new OdetteEndpoint(uri, this, resultConfig);

		return endpoint;
	}

}
