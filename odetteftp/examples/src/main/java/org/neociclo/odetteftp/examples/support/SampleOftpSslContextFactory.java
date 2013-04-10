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
package org.neociclo.odetteftp.examples.support;

import java.security.Security;

import javax.net.ssl.SSLContext;


/**
 * @author Rafael Marins
 */
public class SampleOftpSslContextFactory {

    private static final String PROTOCOL = "TLS";
    private static final SSLContext CLIENT_CONTEXT;

    static {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }

        SSLContext clientContext = null;
        try {
            clientContext = SSLContext.getInstance(PROTOCOL);
            clientContext.init(null, BogusTrustManagerFactory.getTrustManagers(), null);
        } catch (Exception e) {
            throw new Error(
                    "Failed to initialize the client-side SSLContext", e);
        }

        CLIENT_CONTEXT = clientContext;
    }

    public static SSLContext getClientContext() {
    	return CLIENT_CONTEXT;
    }

}
