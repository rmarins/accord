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
package org.neociclo.accord.odetteftp.camel;

import java.util.Calendar;
import java.util.Date;

import org.neociclo.odetteftp.protocol.VirtualFile;

public class FileRenameBean {

	public String renameFile(VirtualFile virtualFile) {
		StringBuilder filename = new StringBuilder();
		filename.append(toHex(convertDateToLong(virtualFile.getDateTime())));
		filename.append('_').append(virtualFile.getOriginator()).append('_');
		filename.append(virtualFile.getDatasetName());

		return filename.toString();
	}

	public long convertDateToLong(Date date) {
		Calendar instance = Calendar.getInstance();
		instance.setTime(date);
		return instance.getTimeInMillis();
	}

	public String toHex(long value) {
		return Long.toString(value, 16);
	}

}
