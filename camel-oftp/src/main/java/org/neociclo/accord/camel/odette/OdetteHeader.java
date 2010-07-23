package org.neociclo.accord.camel.odette;

public enum OdetteHeader {

	OID, PASSWORD, HOST, PORT, DATASET_NAME, FILE_DATE_TIME, ORIGINATOR, DESTINATION, USER_DATA, RECORD_FORMAT, RECORD_SIZE, FILE_SIZE, RESTART_OFFSET, UNIT_COUNT, RECORD_COUNT, FULL_FILE_NAME;

	private static final String PREFIX = "Odette.";
	private static final int indexOf = 7;

	public OdetteHeader fromString(String value) {
		value = value.substring(indexOf);
		return valueOf(value);
	}

	public String toString() {
		return PREFIX.concat(name());
	}

}
