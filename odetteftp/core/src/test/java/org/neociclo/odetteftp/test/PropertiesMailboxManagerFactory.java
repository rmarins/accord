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
package org.neociclo.odetteftp.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class PropertiesMailboxManagerFactory {

    private File file;

    public void setFile(File propertiesFile) {
        this.file = propertiesFile;
    }

    public MailboxManager createMailboxManager() {

        if (file == null) {
            return null;
        }

        Properties props = new Properties();

        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(file);
            props.load(inStream);
        } catch (Throwable t) {
            return null;
        } finally {
            try {
                inStream.close();
            } catch (IOException e) {
                // ignore
            }
        }

        MailboxManager mm = new MailboxManager(props);
        return mm;
    }

}
