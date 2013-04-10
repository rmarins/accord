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
