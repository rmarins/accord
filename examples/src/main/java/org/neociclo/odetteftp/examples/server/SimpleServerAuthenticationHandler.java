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
package org.neociclo.odetteftp.examples.server;

import static org.neociclo.odetteftp.security.PasswordAuthenticationCallback.AuthenticationResult.*;
import static org.neociclo.odetteftp.examples.server.SimpleServerHelper.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.neociclo.odetteftp.examples.support.PasswordAuthenticationHandler;
import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.security.PasswordAuthenticationCallback.AuthenticationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
class SimpleServerAuthenticationHandler extends PasswordAuthenticationHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleServerAuthenticationHandler.class);

	private File serverBaseDir;
	private boolean useMd5Digest;

	public SimpleServerAuthenticationHandler(File serverDataDir) {
		this(serverDataDir, false);
	}

	public SimpleServerAuthenticationHandler(File serverDataDir, boolean useMd5Digest) {
		super();
		this.serverBaseDir = serverDataDir;
		this.useMd5Digest = useMd5Digest;
	}

	@Override
	public AuthenticationResult authenticate(String authenticatingUser, String authenticatingPassword) throws IOException {

		LOGGER.trace("Authenticating user: {}", authenticatingUser);

		File cfile = getUserConfigFile(serverBaseDir, authenticatingUser);

		if (!cfile.exists()) {
			LOGGER.warn("User mailbox structure doesn't exist: {}", cfile);
			return UNKNOWN_USER;
		}

		Properties conf = new Properties();
		conf.load(new FileInputStream(cfile));

		String pwd = (String) conf.get("password");

		if (pwd == null) {
			LOGGER.warn("No user password were set in config file: {}", cfile);
			return INVALID_PASSWORD;
		}

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
