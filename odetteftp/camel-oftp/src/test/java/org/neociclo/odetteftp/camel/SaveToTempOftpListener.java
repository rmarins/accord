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

import java.io.File;
import java.io.IOException;

import org.neociclo.odetteftp.protocol.DefaultStartFileResponse;
import org.neociclo.odetteftp.oftplet.StartFileResponse;
import org.neociclo.odetteftp.protocol.AnswerReason;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.support.OftpletEventListenerAdapter;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class SaveToTempOftpListener extends OftpletEventListenerAdapter {

	@Override
	public StartFileResponse acceptStartFile(VirtualFile vf) {
		File tempFile;
		try {
			tempFile = File.createTempFile("oftp-", "-in.data");
		} catch (IOException e) {
			return DefaultStartFileResponse.negativeStartFileAnswer(AnswerReason.ACCESS_METHOD_FAILURE,
					e.getMessage(), true);
		}
		tempFile.deleteOnExit();
		return DefaultStartFileResponse.positiveStartFileAnswer(tempFile);
	}

}
