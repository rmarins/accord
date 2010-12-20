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

import java.io.Serializable;

import org.neociclo.odetteftp.support.OdetteFtpConfiguration;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class AccountInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String userCode;
	private String password;

	private OdetteFtpConfiguration customConfiguration;

	public AccountInfo() {
		super();
	}

	public AccountInfo(String user, String pswd) {
		super();
		setUserCode(user);
		setPassword(pswd);
	}

	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public OdetteFtpConfiguration getCustomConfiguration() {
		return customConfiguration;
	}

	public void setCustomConfiguration(OdetteFtpConfiguration customConfiguration) {
		this.customConfiguration = customConfiguration;
	}

}
