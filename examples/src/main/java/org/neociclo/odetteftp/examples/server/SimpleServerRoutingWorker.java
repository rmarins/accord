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