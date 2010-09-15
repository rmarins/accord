package org.neociclo.accord.odetteftp.camel;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
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
