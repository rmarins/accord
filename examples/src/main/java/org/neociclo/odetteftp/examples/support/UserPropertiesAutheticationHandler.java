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
package org.neociclo.odetteftp.examples.support;

import static org.neociclo.odetteftp.security.PasswordAuthenticationCallback.AuthenticationResult.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.security.PasswordAuthenticationCallback.AuthenticationResult;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class UserPropertiesAutheticationHandler extends PasswordAuthenticationHandler {

	private Properties users;
	private boolean useMd5Digest;

	public UserPropertiesAutheticationHandler(File propertiesFile) {
		this(propertiesFile, false);
	}

	public UserPropertiesAutheticationHandler(File propertiesFile, boolean useMd5Digest) {
		super();
		this.users = loadUsers(propertiesFile);
		this.useMd5Digest = useMd5Digest;
	}

	private Properties loadUsers(File file) {

		if (file == null) {
			throw new NullPointerException("file"); 
		}

		Properties p = new Properties();

		try {
			FileInputStream in;
			in = new FileInputStream(file);
			p.load(in);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot load the specified users file: " + file, e);
		}

		return p;
	}

	@Override
	public AuthenticationResult authenticate(String authenticatingUser, String authenticatingPassword) throws IOException {

		String oid = authenticatingUser.toUpperCase();
		if (!users.containsKey(oid)) {
			return UNKNOWN_USER;
		}

		String pwd = users.getProperty(oid);

		boolean passwordMatch = false;
		if (useMd5Digest) {
			String passwordHash;
			try {
				passwordHash = hash(authenticatingPassword);
			} catch (NoSuchAlgorithmException e) {
				throw new IOException("Failed to generate MD5 digest over the password.", e);
			}
			passwordMatch = (passwordHash.equals(pwd));
		} else {
			passwordMatch = (authenticatingPassword.equalsIgnoreCase(pwd));
		}

		return (passwordMatch ? SUCCESS : INVALID_PASSWORD);
	}

	private String hash(String text) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(text.getBytes(CommandExchangeBuffer.DEFAULT_PROTOCOL_CHARSET));
		byte[] digest = md.digest();
		return toHexString(digest);
	}

	private String toHexString(byte[] digest) {
		StringBuffer sb = new StringBuffer();
		for (byte d : digest) {
			String hex = Integer.toHexString((int) (d & 0xff));
			if (hex.length() == 1) {
				sb.append('0').append(hex);
			} else {
				sb.append(hex);
			}
		}
		return sb.toString();
	}
}
