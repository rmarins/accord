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
