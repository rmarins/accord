package org.neociclo.accord.camel.odette;

public enum OdetteProtocol {

	OFTP(3305), OFTPS(6619);

	private final int defaultPort;

	OdetteProtocol(int defaultPort) {
		this.defaultPort = defaultPort;
	}

	public int defaultPort() {
		return defaultPort;
	}

}
