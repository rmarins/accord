/**
 * The Accord Project, http://accordproject.org
 * Copyright (C) 2005-2013 Rafael Marins, http://rafaelmarins.com
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
 */
package org.neociclo.odetteftp.examples.support;

import static java.util.concurrent.TimeUnit.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.support.OftpletEventListenerAdapter;

/**
 * @author Rafael Marins
 */
public class SessionFinalizationListener extends OftpletEventListenerAdapter {

	private final Object lock = new Object();

	private final AtomicInteger noInits = new AtomicInteger();
	private final AtomicInteger noDestroys = new AtomicInteger();

	private int noOfSessions;

	public SessionFinalizationListener(int noOfSessions) {
		super();
		this.noOfSessions = noOfSessions;
	}

	@Override
	public void init(OdetteFtpSession session) throws OdetteFtpException {
		synchronized (noInits) {
			noInits.incrementAndGet();
		}
	}

	@Override
	public void destroy() {
		synchronized (noDestroys) {
			int deltaStarts = noInits.get();
			int deltaEnds = noDestroys.incrementAndGet();
			if (deltaStarts >= noOfSessions && deltaEnds >= noOfSessions) {
				synchronized (lock) {
					lock.notifyAll();
				}
			}
		}
	}

	public void waitFinalization() throws InterruptedException {
		synchronized (lock) {
			lock.wait(MINUTES.toMillis(5));
		}
	}
}
