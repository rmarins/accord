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

import java.util.Properties;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class MailboxManager {

    public static final String OFTP_PSWD_PREFIX = "accord.oftp.";

    public static final String OFTP_PSWD_SUFFIX = ".password";

    public static String createPropertyName(String prefix, String mailbox, String suffix) {
        StringBuffer sb = new StringBuffer(prefix);
        sb.append(mailbox.toLowerCase()).append(suffix);
        return sb.toString();
    }

    private Properties properties;

    private boolean ingoreCasePassword;

    public MailboxManager(Properties props) {
        super();
        this.properties = props;
    }

    public boolean existsMailbox(String oid) {
        String pname = createPropertyName(OFTP_PSWD_PREFIX, oid, OFTP_PSWD_SUFFIX);
        return properties.containsKey(pname);
    }

    public boolean checkPassword(String oid, String pwd) {

        String pname = createPropertyName(OFTP_PSWD_PREFIX, oid, OFTP_PSWD_SUFFIX);
        String mgrPassword = properties.getProperty(pname);

        boolean check;
        if (isIngoreCasePassword()) {
            check = (pwd != null && !"".equals(pwd) && pwd.equalsIgnoreCase(mgrPassword));
        } else {
            check = (pwd != null && !"".equals(pwd) && pwd.equals(mgrPassword));
        }

        return check;

    }

    public boolean isIngoreCasePassword() {
        return ingoreCasePassword;
    }

    public void setIngoreCasePassword(boolean ingoreCasePassword) {
        this.ingoreCasePassword = ingoreCasePassword;
    }

}
