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
package org.neociclo.odetteftp.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class TimestampTicker {

	public static final int MAX_COUNTER_VALUE = 9999;

	private static TimestampTicker singleton;

	public static TimestampTicker getInstance() {
		if (singleton == null) {
			singleton = new TimestampTicker();
		}
		return singleton;
	}

	private long lastCallTime;
	private AtomicInteger counter;

	private TimestampTicker() {
		super();
		this.counter = new AtomicInteger();
	}

	public int incrementAndGet() {
		long currentTime = System.currentTimeMillis();
		int value = 0;
		synchronized (counter) {
			value = counter.incrementAndGet();
			if (value >= MAX_COUNTER_VALUE || areSecondsDifferent(lastCallTime, currentTime)) {
				value = 1;
				counter.set(value);
			}
		}
		lastCallTime = currentTime;
		return value;
	}

	private static boolean areSecondsDifferent(long t1, long t2) {
		t1 = (t1 / 1000) * 1000;
		t2 = (t2 / 1000) * 1000;
		return (t1 != t2);
	}

	

}
