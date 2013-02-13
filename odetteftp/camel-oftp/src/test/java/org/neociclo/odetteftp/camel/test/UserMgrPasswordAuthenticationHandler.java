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

import java.io.IOException;

import org.neociclo.odetteftp.protocol.EndSessionReason;
import org.neociclo.odetteftp.support.PasswordAuthenticationHandler;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class UserMgrPasswordAuthenticationHandler extends PasswordAuthenticationHandler {

	private IUserManager userManager;
	private EndSessionReason cause;

	public UserMgrPasswordAuthenticationHandler(IUserManager userMgr) {
		super();
		this.userManager = userMgr;
	}

	@Override
	public boolean authenticate(String oid, String pwd) throws IOException {

		AccountInfo account = userManager.getAccount(oid);
		if (account == null) {
			cause = EndSessionReason.UNKNOWN_USER_CODE;
			return false;
		}

		if (pwd.equals(account.getPassword())) {
			return true;
		} else {
			cause = EndSessionReason.INVALID_PASSWORD;
			return false;
		}

	}

	@Override
	public EndSessionReason getCause() {
		return cause;
	}

}
