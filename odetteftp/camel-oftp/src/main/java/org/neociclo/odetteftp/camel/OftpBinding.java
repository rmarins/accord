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

import static org.neociclo.odetteftp.camel.OftpMessage.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.neociclo.odetteftp.EntityType;
import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.protocol.DeliveryNotification;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.protocol.v20.EnvelopedVirtualFile;
import org.neociclo.odetteftp.protocol.v20.SignedDeliveryNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 */
public class OftpBinding {

	private static final Logger LOGGER = LoggerFactory.getLogger(OftpBinding.class);

	private Endpoint endpoint;

	public OftpBinding(Endpoint endpoint) {
		super();
		this.endpoint = endpoint;
	}

	public Object extractBodyFromOdetteFtpObject(Exchange exchange, OftpMessage message) {
		OdetteFtpObject object = message.getOftpObject();
		return object;
	}

	public Map<String, Object> extractHeadersFromOdetteFtpObject(OdetteFtpObject object,
			Exchange exchange) {

		HashMap<String, Object> m = new HashMap<String, Object>();
		// OdetteFtpObject basic attributes
		m.put(OFTP_DATASET_NAME, object.getDatasetName());
		m.put(OFTP_TIMESTAMP, object.getDateTime());
		m.put(OFTP_TIMESTAMP_TICKER, object.getTicker());
		m.put(OFTP_USER_DATA, object.getUserData());
		m.put(OFTP_DESTINATION, object.getDestination());
		m.put(OFTP_ORIGINATOR, object.getOriginator());

		if (object instanceof VirtualFile) {
			VirtualFile vf = (VirtualFile) object;
			m.put(OFTP_VF_RECORD_FORMAT, vf.getRecordFormat());
			m.put(OFTP_VF_RECORD_SIZE, vf.getRecordSize());
			m.put(OFTP_VF_RESTART_OFFSET, vf.getRestartOffset());
			m.put(OFTP_VF_SIZE, vf.getSize());
		}

		if (object instanceof EnvelopedVirtualFile) {
			EnvelopedVirtualFile env = (EnvelopedVirtualFile) object;
			m.put(OFTP_VF_CIPHER_SUITE, env.getCipherSuite());
			m.put(OFTP_VF_COMPRESSION_ALGORITHM, env.getCompressionAlgorithm());
			m.put(OFTP_VF_ENVELOPING_FORMAT, env.getEnvelopingFormat());
			m.put(OFTP_VF_DESCRIPTION, env.getFileDescription());
			m.put(OFTP_VF_ORIGINAL_SIZE, env.getOriginalFileSize());
			m.put(OFTP_VF_SECURITY_LEVEL, env.getSecurityLevel());
			m.put(OFTP_VF_SIGNED_NOTIFICATION_REQUEST, env.isSignedNotificationRequest());
		}

		if (object instanceof DeliveryNotification) {
			DeliveryNotification notif = (DeliveryNotification) object;
			m.put(OFTP_NOTIF_CREATOR, notif.getCreator());
			m.put(OFTP_NOTIF_REASON, notif.getReason());
			m.put(OFTP_NOTIF_REASON_TEXT, notif.getReasonText());
			m.put(OFTP_NOTIF_TYPE, notif.getType());
		}

		if (object instanceof SignedDeliveryNotification) {
			SignedDeliveryNotification signed = (SignedDeliveryNotification) object;
			m.put(OFTP_NOTIF_SIGNATURE, signed.getNotificationSignature());
			m.put(OFTP_NOTIF_FILE_HASH, signed.getVirtualFileHash());
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Headers extracted from the Odette FTP Object: {}", m);
		}

		return m;
	}

	public Map<String, Object> extractHeadersFromOdetteFtpSession(OdetteFtpSession session,
			Exchange exchange) {

		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put(OFTP_SESSION_DEB_SIZE, session.getDataBufferSize());
		m.put(OFTP_SESSION_WINDOW_SIZE, session.getWindowSize());
		m.put(OFTP_SESSION_VERSION, session.getVersion());
		m.put(OFTP_SESSION_USER_DATA, session.getUserData());
		m.put(OFTP_SESSION_COMPRESSION, session.isCompressionSupported());
		m.put(OFTP_SESSION_RESTART, session.isRestartSupported());
		m.put(OFTP_SESSION_SPECIAL_LOGIC, session.hasSpecialLogic());
		m.put(OFTP_SESSION_SECURE_AUTHENTICATION, session.useSecureAuthentication());
		m.put(OFTP_SESSION_CIPHER_SUITE_SELECTION, session.getCipherSuiteSelection());

		if (session.getEntityType() == EntityType.RESPONDER) {
			// server
			m.put(OFTP_SESSION_LOCAL_USER, session.getResponseUser());
			m.put(OFTP_SESSION_REMOTE_USER, session.getUserCode());
		} else {
			// client
			m.put(OFTP_SESSION_LOCAL_USER, session.getUserCode());
			m.put(OFTP_SESSION_REMOTE_USER, session.getResponseUser());
		}
		return m;
	}

	public Map<String, Object> extractHeadersFromOftpEvent(OftpMessage message) {
		HashMap<String, Object> m = new HashMap<String, Object>();
		OftpEvent event = message.getBody(OftpEvent.class);
		if (event != null) {
			m.put(OFTP_MESSAGE_EVENT_NAME, event.getEventName());
		}
		return m;
	}

	public Map<String, Object> extractHeadersFromOftpCommand(OftpMessage message) {
		HashMap<String, Object> m = new HashMap<String, Object>();
		OftpCommand cmd = message.getBody(OftpCommand.class);
		if (cmd != null) {
			m.put(OFTP_MESSAGE_COMMAND_NAME, cmd.getCommandName());
			m.put(OFTP_REPLY_TO, endpoint.getEndpointUri());
		}
		return m;
	}

}
