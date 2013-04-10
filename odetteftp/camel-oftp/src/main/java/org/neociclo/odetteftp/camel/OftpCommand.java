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
package org.neociclo.odetteftp.camel;

import java.io.Serializable;

import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 */
public class OftpCommand implements Serializable {

	public static final String OFTP_CMD_RETRIEVE_USER_OUTGOING_EXCHANGES = "retrieveUserOutgoingExchangesRequest";
	public static final String OFTP_CMD_ACCEPT_INCOMING_FILE = "acceptIncomingFileRequest";

	public static OftpCommand retrieveUserOutgoingExchangesCommand() {
		return new OftpCommand(OFTP_CMD_RETRIEVE_USER_OUTGOING_EXCHANGES);
	}

	public static OftpCommand acceptIncomingFileCommand(VirtualFile incomingFile) {
		return new OftpCommand(OFTP_CMD_ACCEPT_INCOMING_FILE, incomingFile);
	}

	private static final long serialVersionUID = 1L;

	private String commandName;
	private OdetteFtpObject requestObject;

	public OftpCommand(String commandName) {
		this(commandName, null);
	}

	public OftpCommand(String commandName, OdetteFtpObject requestObject) {
		super();
		this.commandName = commandName;
		setRequestObject(requestObject);
	}

	public OdetteFtpObject getRequestObject() {
		return requestObject;
	}

	public void setRequestObject(OdetteFtpObject requestObject) {
		this.requestObject = requestObject;
	}

	public String getCommandName() {
		return commandName;
	}

}
