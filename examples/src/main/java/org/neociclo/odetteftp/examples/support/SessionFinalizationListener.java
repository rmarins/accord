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
package org.neociclo.odetteftp.examples.support;

import static java.util.concurrent.TimeUnit.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.neociclo.odetteftp.OdetteFtpException;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.support.OftpletEventListenerAdapter;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
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
