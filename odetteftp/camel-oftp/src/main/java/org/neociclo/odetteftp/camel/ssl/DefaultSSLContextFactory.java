/**
 * Neociclo Accord, Open Source B2B Integration Suite
 * Copyright (C) 2005-2010 Neociclo, http://www.neociclo.com
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
