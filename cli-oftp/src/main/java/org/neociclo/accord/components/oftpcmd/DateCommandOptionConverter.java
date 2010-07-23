package org.neociclo.accord.components.oftpcmd;

import java.util.Calendar;
import java.util.Date;

/**
 * Parse date based on format MMddHHmm[yy][.ss]
 * 
 * @author bruno
 * 
 */
public class DateCommandOptionConverter implements CommandOptionConverter<Date> {

	public Date convert(String... optionArguments) throws Exception {
		String value = optionArguments[0];

		Calendar cal = Calendar.getInstance();

		try {
			cal.set(Calendar.MONTH, Integer.valueOf(value.substring(0, 2)) - 1);
			cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(value
					.substring(2, 4)));
			cal.set(Calendar.HOUR_OF_DAY, Integer
					.valueOf(value.substring(4, 6)));
			cal.set(Calendar.MINUTE, Integer.valueOf(value.substring(6, Math
					.min(8, value.length()))));

			if (value.length() > 8 && value.charAt(8) != '.') {
				cal.set(Calendar.YEAR, Integer.valueOf(value.substring(8, 10)));
			}

			if (value.indexOf('.') != -1) {
				cal.set(Calendar.SECOND, Integer.valueOf(value.substring(value
						.indexOf('.') + 1)));
			}
		} catch (IndexOutOfBoundsException e) {
			throw new RuntimeException("Could not parse date: " + value
					+ " with format MMddHHmm[yy][.ss]", e);
		}

		return cal.getTime();
	}
}
