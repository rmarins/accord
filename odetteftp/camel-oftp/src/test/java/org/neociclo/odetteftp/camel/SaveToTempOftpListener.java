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
