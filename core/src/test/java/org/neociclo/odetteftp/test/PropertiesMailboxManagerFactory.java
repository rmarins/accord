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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
