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
package org.neociclo.odetteftp.util;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class SignatureVerifyResult {

	private Exception error;
	private boolean done;
	private boolean success;

	private void setDone() {
		this.done = true;
	}

	public boolean isDone() {
		return done;
	}

	void setError(Exception e) {
		setDone();
		this.error = e;
	}

	/**
	 * Usually one of these errors: CMSException, CertificateExpiredException,
	 * CertificateNotYetValidException, NoSuchAlgorithmException,
	 * NoSuchProviderException
	 * 
	 * @return
	 */
	public Exception getExceptionCaught() {
		return error;
	}

	void setSuccess() {
		setDone();
		success = true;
	}

	void setFailure() {
		setDone();
		success = false;
	}

	public boolean hasSucceed() {
		return success && isDone();
	}

	public boolean hasFailed() {
		return (!success) && isDone();
	}
}
