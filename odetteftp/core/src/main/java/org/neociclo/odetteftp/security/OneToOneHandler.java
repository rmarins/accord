/**
 * Neociclo Accord, Open Source B2Bi Middleware
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
package org.neociclo.odetteftp.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;

/**
 * Define the handler for a specific Callback type.
 * 
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public interface OneToOneHandler<T extends Callback> {

	/**
	 * 
	 * @param cb
	 * @throws IOException
	 *             if an input or output error occur.
	 */
	void handle(T cb) throws IOException;

}
