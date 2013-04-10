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

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.neociclo.odetteftp.camel.FileRenameBean;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;

public class TestFileRenameBean {
//
//	@Test
//	public void testToHex() {
//		String hex = new FileRenameBean().toHex(Long.MAX_VALUE);
//
//		Assert.assertTrue(hex.equals(Long.toString(Long.MAX_VALUE, 16)));
//	}
//
//	@Test
//	public void testConvertDate() {
//		Date date = Calendar.getInstance().getTime();
//		long time = date.getTime();
//
//		long timeToCompare = new FileRenameBean().convertDateToLong(date);
//
//		Assert.assertTrue(timeToCompare == time);
//	}

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
