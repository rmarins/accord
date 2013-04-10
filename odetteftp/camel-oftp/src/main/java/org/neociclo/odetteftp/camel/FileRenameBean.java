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
package org.neociclo.odetteftp.camel;

import java.text.SimpleDateFormat;

import org.neociclo.odetteftp.protocol.VirtualFile;

public class FileRenameBean {

	private static final SimpleDateFormat TIMESTAMP = new SimpleDateFormat("yyyyMMddHHmmss");

	private static final char SEP = '$';

	public String renameFile(VirtualFile vf) {
		StringBuilder filename = new StringBuilder();
		filename.append(TIMESTAMP.format(vf.getDateTime()));
		filename.append(SEP).append(vf.getDestination());
		filename.append(SEP).append(vf.getOriginator());
		filename.append(SEP).append(vf.getDatasetName());

		return filename.toString();
	}

}
