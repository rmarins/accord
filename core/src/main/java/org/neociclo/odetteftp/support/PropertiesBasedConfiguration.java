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
package org.neociclo.odetteftp.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class PropertiesBasedConfiguration extends OdetteFtpConfiguration {

	public static final String TRANSFER_MODE_PROP = "transferMode";
	public static final String DATA_EXCHANGE_BUFFER_PROP = "dataExchangeBuffer";
	public static final String WINDOW_PROP = "window";
	public static final String COMPRESSION_PROP = "compression";
	public static final String RESTART_PROP = "restart";
	public static final String SPECIAL_LOGIC_PROP = "specialLogic";
	public static final String SECURE_AUTHENTICATION_PROP = "secureAuthentication";
	public static final String CIPHER_SUITE_PROP = "cipherSuite";
	public static final String USER_DATA_PROP = "userData";
	public static final String VERSION_PROP = "version";

	private static final long serialVersionUID = 1L;

	public void loadFromXml(InputStream inStream) throws IOException {
		Properties properties = new Properties();
		properties.loadFromXML(inStream);

		properties2config(properties);
	}

	public void load(InputStream inStream) throws IOException {
		Properties properties = new Properties();
		properties.load(inStream);

		properties2config(properties);
	}

	public void load(Reader reader) throws IOException {
		Properties properties = new Properties();
		properties.load(reader);

		properties2config(properties);
	}

	public void storeToXml(OutputStream outStream, String comments) throws IOException {
		storeToXml(outStream, comments, null);
	}

	public void storeToXml(OutputStream outStream, String comments, String encoding) throws IOException {
		Properties properties = new Properties();
		config2properties(properties);

		properties.storeToXML(outStream, comments, encoding);
	}

	public void store(OutputStream outStream, String comments) throws IOException {
		Properties properties = new Properties();
		config2properties(properties);

		properties.store(outStream, comments);
	}

	public void store(Writer writer, String comments) throws IOException {
		Properties properties = new Properties();
		config2properties(properties);

		properties.store(writer, comments);
	}

	private void properties2config(Properties props) {

		String textTransferMode = props.getProperty(TRANSFER_MODE_PROP);
		if (textTransferMode != null) {
			setTransferMode(TransferMode.valueOf(textTransferMode));
		}

		String textDeb = props.getProperty(DATA_EXCHANGE_BUFFER_PROP);
		if (textDeb != null) {
			setDataExchangeBufferSize(Integer.parseInt(textDeb));
		}

		String textCredit = props.getProperty(WINDOW_PROP);
		if (textCredit != null) {
			setWindowSize(Integer.parseInt(textCredit));
		}

		String textCompression = props.getProperty(COMPRESSION_PROP);
		if (textCompression != null) {
			useCompression(Boolean.parseBoolean(textCompression));
		}

		String textRestart = props.getProperty(RESTART_PROP);
		if (textRestart != null) {
			useRestart(Boolean.parseBoolean(textRestart));
		}

		String textSpecialLogic = props.getProperty(SPECIAL_LOGIC_PROP);
		if (textSpecialLogic != null) {
			hasSpecialLogic(Boolean.parseBoolean(textSpecialLogic));
		}

		String textSecureAuth = props.getProperty(SECURE_AUTHENTICATION_PROP);
		if (textSecureAuth != null) {
			setUseSecureAuthentication(Boolean.parseBoolean(textSecureAuth));
		}

		String textCipherSuite = props.getProperty(CIPHER_SUITE_PROP);
		if (textCipherSuite != null) {
			setCipherSuiteSelection(CipherSuite.valueOf(textCipherSuite));
		}

		String textUserData = props.getProperty(USER_DATA_PROP);
		if (textUserData != null) {
			setUserData(textUserData);
		}

		String textVersion = props.getProperty(VERSION_PROP);
		if (textVersion != null) {
			setVersion(OdetteFtpVersion.valueOf(textVersion));
		}

	}

	private void config2properties(Properties props) {
		if (getTransferMode() != null) {
			props.put(TRANSFER_MODE_PROP, getTransferMode().name());
		}
		if (getDataExchangeBufferSize() != null) {
			props.put(DATA_EXCHANGE_BUFFER_PROP, getDataExchangeBufferSize().toString());
		}
		if (getWindowSize() != null) {
			props.put(WINDOW_PROP, getWindowSize().toString());
		}
		if (getUseCompression() != null) {
			props.put(COMPRESSION_PROP, getUseCompression().toString());
		}
		if (getUseRestart() != null) {
			props.put(RESTART_PROP, getUseRestart().toString());
		}
		if (getHasSpecialLogic() != null) {
			props.put(SPECIAL_LOGIC_PROP, getHasSpecialLogic().toString());
		}
		if (useSecureAuthentication() != null) {
			props.put(SECURE_AUTHENTICATION_PROP, useSecureAuthentication().toString());
		}
		if (getCipherSuiteSelection() != null) {
			props.put(CIPHER_SUITE_PROP, getCipherSuiteSelection().toString());
		}
		if (getUserData() != null) {
			props.put(USER_DATA_PROP, getUserData());
		}
		if (getVersion() != null) {
			props.put(VERSION_PROP, getVersion().name());
		}
	}
}
