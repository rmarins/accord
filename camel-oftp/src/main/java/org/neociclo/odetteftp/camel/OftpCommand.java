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
package org.neociclo.odetteftp.camel;

import java.io.Serializable;

import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
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
