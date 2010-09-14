package org.neociclo.accord.odetteftp.camel;

public enum OdetteTransport {

	TCP(true), ISDN(false), X25(false);

	/**
	 * <p>
	 * <b>String Transmission Buffer</b> is always true for TCP and false for
	 * X25 and ISDN.
	 * </p>
	 * 
	 */
	private boolean stb;

	OdetteTransport(boolean stb) {
		this.stb = stb;
	}

	public boolean isStb() {
		return stb;
	}

}
