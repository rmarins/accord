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
package org.neociclo.odetteftp.examples.server;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.util.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SimpleServerRoutingWorker {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleServerRoutingWorker.class);

	private static class MakeDeliveryTask implements Runnable {
	
		private String userCode;
		private OdetteFtpObject obj;
		private File baseDir;
	
		public MakeDeliveryTask(File baseDir, String userCode, OdetteFtpObject obj) {
			super();
			this.baseDir = baseDir;
			this.userCode = userCode;
			this.obj = obj;
		}
	
		public void run() {
	
			String filename = SimpleServerHelper.createFileName(obj);
	
			File sourceDir = SimpleServerHelper.getUserWorkDir(baseDir, userCode);
			File sourceFile = new File(sourceDir, filename);
	
			String recipientOid = obj.getDestination();
			File destDir = SimpleServerHelper.getUserMailboxDir(baseDir, recipientOid);
			File destFile = new File(destDir, filename);
	
			if (destFile.exists()) {
				LOGGER.warn("Delivery failed. Duplicate file in recipient mailbox. This is a simple server " +
						"implementation and it doesn't support delivery retries. Overwriting file: {}", destFile);
			}

			try {
				LOGGER.trace("EXISTS: {}", sourceFile.exists());
				IoUtil.move(sourceFile, destFile);
				LOGGER.info("Delivered to [{}]: ", recipientOid, obj);
			} catch (IOException e) {
				LOGGER.info("Delivery failed. Cannot move object file to the recipient box [{}]: {}", recipientOid,
						sourceFile);
				LOGGER.error("Routing failed.", e);
				return;
			}
	
		}
		
	}

	private ExecutorService executor;

	public SimpleServerRoutingWorker() {
		super();
		this.executor = Executors.newCachedThreadPool();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				executor.shutdown();
			}
		}));
	}

	public void deliver(File baseDir, String userCode, OdetteFtpObject obj) {

		SimpleServerRoutingWorker.MakeDeliveryTask task = new MakeDeliveryTask(baseDir, userCode, obj);
		executor.submit(task);

	}
}