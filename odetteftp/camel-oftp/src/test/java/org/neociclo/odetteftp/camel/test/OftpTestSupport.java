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
