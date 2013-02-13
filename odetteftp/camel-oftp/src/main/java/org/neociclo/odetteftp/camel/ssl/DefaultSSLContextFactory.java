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
 */package org.neociclo.odetteftp.camel.ssl;

import java.io.File;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.camel.converter.IOConverter;

/**
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class DefaultSSLContextFactory implements SSLContextFactory {

	private String SSL_PROTOCOL = "TLS";
	private static SSLContext sslContext;

	public SSLContextFactory setup(String keyStoreFormat, String securityProvider, File keyStoreFile,
			File trustStoreFile, char[] passphrase) throws Exception {

		KeyManager[] keyManagers = null;

		if (keyStoreFile != null) {
			KeyStore ks = KeyStore.getInstance(keyStoreFormat);
	
			ks.load(IOConverter.toInputStream(keyStoreFile), passphrase);
	
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(securityProvider);
			kmf.init(ks, passphrase);
		}

		sslContext = SSLContext.getInstance(SSL_PROTOCOL);

		if (trustStoreFile != null) {
			KeyStore ts = KeyStore.getInstance(keyStoreFormat);
			ts.load(IOConverter.toInputStream(trustStoreFile), passphrase);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(securityProvider);
			tmf.init(ts);
			sslContext.init(keyManagers, tmf.getTrustManagers(), null);
		} else {
			sslContext.init(keyManagers, null, null);
		}

		return this;
	}

	public SSLContext createSSLContext() {
		return sslContext;
	}

}
