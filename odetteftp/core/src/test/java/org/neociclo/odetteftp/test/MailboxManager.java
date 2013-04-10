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
package org.neociclo.odetteftp.test;

import java.util.Properties;

/**
 * @author Rafael Marins
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
