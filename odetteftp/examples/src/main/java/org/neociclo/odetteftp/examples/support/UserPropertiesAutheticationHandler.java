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
package org.neociclo.odetteftp.examples.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.EndSessionReason;
import org.neociclo.odetteftp.support.PasswordAuthenticationHandler;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class UserPropertiesAutheticationHandler extends PasswordAuthenticationHandler {

	private Properties users;
	private boolean useMd5Digest;
	private EndSessionReason cause;

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
	public boolean authenticate(String authenticatingUser, String authenticatingPassword) throws IOException {

		String oid = authenticatingUser.toUpperCase();
		if (!users.containsKey(oid)) {
			cause = EndSessionReason.UNKNOWN_USER_CODE;
			return false;
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

		if (passwordMatch) {
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
