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
