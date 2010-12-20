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
package org.neociclo.odetteftp.camel.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Random;

import org.apache.camel.test.CamelTestSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpTestSupport extends CamelTestSupport {

    protected static int port;

	private static final File DEFAULT_TEST_OUTPUT_DIR = new File("./target");

	protected transient Logger logger = LoggerFactory.getLogger(getClass());

	public static void initPort() throws Exception {
        File file = (new File(DEFAULT_TEST_OUTPUT_DIR, "oftpport.txt")).getAbsoluteFile();

        if (!file.exists()) {
            // start from somewhere in the 33050-33950 range
            port = 33050 + new Random().nextInt(900);
        } else {
            // read port number from file
        	BufferedReader br = new BufferedReader(new FileReader(file));
        	try {
        		String s = br.readLine();
        		port = Integer.parseInt(s);
        	} finally {
        		br.close();
        	}
            // use next number
            port++;
        }

        // save to file, do not append
        BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
        try {
            bw.write(String.valueOf(port));
        } finally {
            bw.close();
        }
    }

    public static void resetPort() throws Exception {
        port = 0;
    }

    protected int getPort() {
        return port;
    }

	protected File getOutputDir() {
		return DEFAULT_TEST_OUTPUT_DIR;
	}

	public static URL getResource(String name) {
        return Thread.currentThread().getContextClassLoader().getResource(name);
    }

    public static File getResourceFile(String name) throws URISyntaxException {
        return new File(getResource(name).toURI());
    }

}
