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
package org.neociclo.odetteftp.examples.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.util.ProtocolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class SimpleServerHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleServerHelper.class);

	private static final FilenameFilter EXCHANGES_FILENAME_FILTER = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".vfile") || name.endsWith(".notif"));
		}
	};

	private SimpleServerHelper() {
	}

	public static void createUserDirStructureIfNotExist(String userCode, File serverBaseDir) {

		File dataDir = getServerDataDir(serverBaseDir);
		File mailboxDir = getUserMailboxDir(serverBaseDir, userCode);
		File workDir = getUserWorkDir(serverBaseDir, userCode);

		if (!dataDir.exists()) {
			dataDir.mkdirs();
		}

		if (!mailboxDir.exists()) {
			mailboxDir.mkdirs();
		}

		if (!workDir.exists()) {
			workDir.mkdirs();
		}

	}

	public static String createFileName(OdetteFtpObject obj) {
		StringBuffer sb = new StringBuffer();
		sb.append(obj.getOriginator()).append('$');
		sb.append(obj.getDestination()).append('$');
		sb.append(ProtocolUtil.formatDate("yyyyMMddHHmmSS.sss", obj.getDateTime())).append('$');
		sb.append(obj.getDatasetName());
		if (obj instanceof VirtualFile) {
			sb.append(".vfile");
		} else {
			sb.append(".notif");
		}
		return sb.toString();
	}

	public static File getServerDataDir(File baseDir) {
		return new File(baseDir, "data");
	}

	public static File getUserDir(File baseDir, String userCode) {
		return new File(baseDir, userCode.toLowerCase());
	}

	public static File getUserMailboxDir(File baseDir, String userCode) {
		return new File(getUserDir(baseDir, userCode), "mailbox");
	}

	public static File getUserWorkDir(File baseDir, String userCode) {
		return new File(getUserDir(baseDir, userCode), "work");
	}

	public static File getUserConfigFile(File baseDir, String userCode) {
		return new File(getUserDir(baseDir, userCode), "accord-oftp.conf");
	}

	public static OdetteFtpObject loadObject(File input) throws IOException {
	
		OdetteFtpObject obj = null;
	
		FileInputStream stream = new FileInputStream(input);
		ObjectInputStream os = new ObjectInputStream(stream);
	
		try {
			obj = (OdetteFtpObject) os.readObject();
		} catch (ClassNotFoundException cnfe) {
			LOGGER.error("Cannot load Odette FTP Object file: " + input, cnfe);
		} finally {
			try {
				os.close();
			} catch (Throwable t) {
				// do nothing
			}
			try {
				stream.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
	
		return obj;
	}

	public static void storeObject(File output, OdetteFtpObject obj) throws IOException {
	
		FileOutputStream stream = new FileOutputStream(output);
	
		ObjectOutputStream os = new ObjectOutputStream(stream);
	
		try {
			os.writeObject(obj);
			os.flush();
			stream.flush();
		} finally {
			try {
				os.close();
				stream.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
	
	}

	public static File createDataFile(VirtualFile vf, File serverBaseDir) throws IOException {
		String filename = createFileName(vf);
		File dataDir = getServerDataDir(serverBaseDir);
		return File.createTempFile(filename + "_", null, dataDir);
	}

	public static void storeInWork(String userCode, OdetteFtpObject obj, File serverBaseDir) throws IOException {
		File workDir = getUserWorkDir(serverBaseDir, userCode);
		String filename = createFileName(obj);

		File outputFile = new File(workDir, filename);
		storeObject(outputFile, obj);

	}

	public static void storeInMailbox(String userCode, OdetteFtpObject obj, File serverBaseDir) throws IOException {
		File mailboxDir = getUserMailboxDir(serverBaseDir, userCode);
		String filename = createFileName(obj);

		File outputFile = new File(mailboxDir, filename);
		storeObject(outputFile, obj);

	}

	public static File[] listExchanges(String userCode, File serverBaseDir) {
		File mailboxDir = getUserMailboxDir(serverBaseDir, userCode);
		File[] exchanges = mailboxDir.listFiles(EXCHANGES_FILENAME_FILTER);
		return exchanges;
	}

	public static boolean hasExchange(String userCode, File serverBaseDir) {
		File[] exchanges = listExchanges(userCode, serverBaseDir);
		return (exchanges != null && exchanges.length > 0);
	}

	public static void deleteExchange(String userCode, OdetteFtpObject obj, File serverBaseDir) {
		if (obj instanceof VirtualFile) {
			VirtualFile vf = (VirtualFile) obj;
			File payloadFile = vf.getFile();
			if (payloadFile.exists()) {
				payloadFile.delete();
			}
		}

		File mailboxDir = getUserMailboxDir(serverBaseDir, userCode);
		String filename = createFileName(obj);
		File mailboxFile = new File(mailboxDir, filename);

		if (mailboxFile.exists()) {
			mailboxFile.delete();
		}
	}

}
