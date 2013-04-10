/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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
package org.neociclo.odetteftp.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpTestUtil {

    public static File getBaseDir() {
        // check Maven system prop first and use if set
        String basedir = System.getProperty("basedir");
        if (basedir != null) {
            return new File(basedir);
        } else {
            return new File(".");
        }
    }

    public static File getOutputDir() {
        // check Maven system prop first and use if set
        String outdir = System.getProperty("outputdir");
        if (outdir != null) {
            return new File(outdir);
        } else {
            return new File("./target");
        }
    }

    public static File getTestDataDir() {
        File testData = new File(getOutputDir(), "test-data");
        if (!testData.exists()) {
            if (!testData.mkdirs()) {
                return null;
            }
        }
        return testData;
    }

    public static URL getResource(String name) {
        return Thread.currentThread().getContextClassLoader().getResource(name);
    }

    public static File getResourceFile(String name) throws URISyntaxException {
        return new File(getResource(name).toURI());
    }

}
