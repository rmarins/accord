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
package org.neociclo.odetteftp.examples.client;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.neociclo.odetteftp.examples.MainSupport;
import org.neociclo.odetteftp.examples.support.DefaultOftpletFactory;
import org.neociclo.odetteftp.oftplet.OftpletFactory;
import org.neociclo.odetteftp.security.MappedCallbackHandler;
import org.neociclo.odetteftp.security.PasswordCallback;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.PasswordHandler;
import org.neociclo.odetteftp.util.ExecutorUtil;

/**
 * @author Rafael Marins
 * @version $Rev$
 */
public class ConnectAndDisconnect {

	public static void main(String[] args) throws Exception {

		MainSupport ms = new MainSupport(ConnectAndDisconnect.class, args, "server", "port", "odetteid", "password");

		String server = ms.get(0);
		int port = Integer.parseInt(ms.get(1));
		String userCode = ms.get(2);
		String userPassword = ms.get(3);

		MappedCallbackHandler securityCallbacks = new MappedCallbackHandler();
		securityCallbacks.addHandler(PasswordCallback.class,
				new PasswordHandler(userCode, userPassword));

		Executor bossExecutor = Executors.newCachedThreadPool();
		Executor workerExecutor = Executors.newCachedThreadPool();
		Timer timer = new HashedWheelTimer();

		OftpletFactory factory = new DefaultOftpletFactory(securityCallbacks);

		TcpClient oftp = new TcpClient();
		oftp.setBossExecutor(bossExecutor);
		oftp.setWorkerExecutor(workerExecutor);
		oftp.setTimer(timer);

		try {
			for (int i=0; i<1000; i++) {
				long t0 = System.currentTimeMillis();
	
				try {
					oftp.setOftpletFactory(factory);
					oftp.connect(server, port, true);
				} catch (Exception e) {
					continue;
				}
	
				long t1 = System.currentTimeMillis();
				long delta = t1 - t0;
				if (delta < 1000) {
					Thread.sleep(1000 - delta);
				}

				System.gc();

			}
		} finally {
			timer.stop();
			ExecutorUtil.terminate(bossExecutor, workerExecutor);
		}

	}

}
