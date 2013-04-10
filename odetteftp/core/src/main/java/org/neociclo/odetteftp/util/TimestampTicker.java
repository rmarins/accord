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
