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
package org.neociclo.accord.odetteftp.camel.test;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.neociclo.accord.odetteftp.camel.FileRenameBean;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;

public class TestFileRenameBean {

	@Test
	public void testToHex() {
		String hex = new FileRenameBean().toHex(Long.MAX_VALUE);

		Assert.assertTrue(hex.equals(Long.toString(Long.MAX_VALUE, 16)));
	}

	@Test
	public void testConvertDate() {
		Date date = Calendar.getInstance().getTime();
		long time = date.getTime();

		long timeToCompare = new FileRenameBean().convertDateToLong(date);

		Assert.assertTrue(timeToCompare == time);
	}

	@Test
	public void testFileRenameBean() {
		DefaultVirtualFile dvf = new DefaultVirtualFile();
		dvf.setDatasetName("foo");
		dvf.setOriginator("bar");
		Date time = Calendar.getInstance().getTime();
		dvf.setDateTime(time);

		String name = new FileRenameBean().renameFile(dvf);
		String string = new String(Long.toString(time.getTime(), 16) + "_bar_foo");

		Assert.assertEquals(string, name);
	}
}
