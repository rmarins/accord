/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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
package org.neociclo.odetteftp.test;

import java.io.File;
import java.net.URISyntaxException;

import org.neociclo.odetteftp.service.ServiceConfiguration;
import org.neociclo.odetteftp.util.OftpTestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public abstract class SecureEmbeddedTestTemplate extends EmbeddedTestTemplate {

    public static final int SECURE_OFTP_TESTING_PORT = 16619;

    private static final Logger LOGGER = LoggerFactory.getLogger(SecureEmbeddedTestTemplate.class);

    private static final String TEST_KEYSTORE_PATH = "keystores/server-bogus.p12";

    private static final String TEST_KEYSTORE_PASSWORD = "neociclo";

    @Override
    protected int getServicePort() {
        return SECURE_OFTP_TESTING_PORT;
    }

    /**
     * Configure ODETTE-FTP service configuration for version 2.0 of the
     * protocol.
     */
    @Override
    protected ServiceConfiguration createServiceConfiguration() {
        ServiceConfiguration config = super.createServiceConfiguration();

        // change to secure connection testing port
        config.setPort(getServicePort());

        // set up secure connection
        File ksFile = null;
        try {
            ksFile = OftpTestUtil.getResourceFile(TEST_KEYSTORE_PATH);
        } catch (URISyntaxException e) {
            // ignore
        }

        if (ksFile == null) {
            LOGGER.info("Cannot use keystore file. Skipping SSL configuration...");
        } else {
            config.setSsl(true);
            config.setKeystorePath(ksFile.getAbsolutePath());
            config.setKeystorePassword(TEST_KEYSTORE_PASSWORD);
        }

        return config;
    }

}
