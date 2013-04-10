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
package org.neociclo.odetteftp.protocol.v20;

import org.neociclo.odetteftp.protocol.NormalizedVirtualFile;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class DefaultNormalizedEnvelopedVirtualFile extends DefaultEnvelopedVirtualFile implements NormalizedVirtualFile {

	private static final long serialVersionUID = 1L;

	private VirtualFile originalVirtualFile;

	public DefaultNormalizedEnvelopedVirtualFile(VirtualFile originalVirtualFile) {
		super();
		this.originalVirtualFile = originalVirtualFile;
	}

	public VirtualFile getOriginalVirtualFile() {
		return originalVirtualFile;
	}

}
