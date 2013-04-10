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

import org.neociclo.odetteftp.oftplet.Oftplet;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;

/**
 * @author Rafael Marins
 * @version $Rev$
 */
public class DefaultOftpletFactory implements OftpletFactory {

	private OdetteFtpConfiguration conf;
	private MappedCallbackHandler callbackHandler;

	public DefaultOftpletFactory(MappedCallbackHandler callbackHandler) {
		this(new OdetteFtpConfiguration(), callbackHandler);
	}

	public DefaultOftpletFactory(OdetteFtpConfiguration conf, MappedCallbackHandler callbackHandler) {
		this.conf = conf;
		this.callbackHandler = callbackHandler;
	}

	public Oftplet createProvider() {
		Oftplet oftplet = new DefaultOftplet(conf, callbackHandler);
		return oftplet;
	}

}
