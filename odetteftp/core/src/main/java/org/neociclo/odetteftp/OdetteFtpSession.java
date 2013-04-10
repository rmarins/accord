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
package org.neociclo.odetteftp;

import static org.neociclo.odetteftp.EntityState.*;
import static org.neociclo.odetteftp.EntityType.*;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.neociclo.odetteftp.oftplet.ChannelCallback;
import org.neociclo.odetteftp.protocol.CommandIdentifier;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;
import org.neociclo.odetteftp.util.ProtocolUtil;
import org.neociclo.odetteftp.util.AttributeKey;

/**
 * @author Rafael Marins
 */
public class OdetteFtpSession {

    private static final AttributeKey BUFFER_COMPRESSION_ATTR = new AttributeKey(OdetteFtpSession.class,
            "odette-ftp.compression");

    private static final AttributeKey OUTGOING_CREDITS_COUNTER_ATTR = new AttributeKey(OdetteFtpSession.class,
    "odette-ftp.outgoing.credits-counter");

    private static final AttributeKey INCOMING_CREDITS_COUNTER_ATTR = new AttributeKey(OdetteFtpSession.class,
    "odette-ftp.incoming.credits-counter");

    private static final AttributeKey OUTGOING_TRANSFER_OFFSET_ATTR = new AttributeKey(OdetteFtpSession.class,
    "odette-ftp.outgoing.offset");

    private static final AttributeKey INCOMING_TRANSFER_OFFSET_ATTR = new AttributeKey(OdetteFtpSession.class,
    "odette-ftp.incoming.offset");

    private static final AttributeKey RESPONSE_USER_CODE_ATTR = new AttributeKey(OdetteFtpSession.class,
            "odette-ftp.response.user-code");

    private static final AttributeKey RESPONSE_USER_DATA_ATTR = new AttributeKey(OdetteFtpSession.class,
            "odette-ftp.response.user-data");

    private static final AttributeKey RESTART_ATTR = new AttributeKey(OdetteFtpSession.class, "odette-ftp.restart");

    private static final AttributeKey SDEB_ATTR = new AttributeKey(OdetteFtpSession.class,
            "odette-ftp.data-buffer-size");

    private static final AttributeKey SPECIAL_LOGIC_ATTR = new AttributeKey(OdetteFtpSession.class,
            "odette-ftp.special-logic");

    private static final AttributeKey STATE_ATTR = new AttributeKey(OdetteFtpSession.class, "odette-ftp.state");

    private static final AttributeKey MODE_ATTR = new AttributeKey(OdetteFtpSession.class, "odette-ftp.transfer-mode");

    private static final AttributeKey TIMEOUT_ATTR = new AttributeKey(OdetteFtpSession.class, "odette-ftp.timeout");

    private static final AttributeKey USER_CODE_ATTR = new AttributeKey(OdetteFtpSession.class, "odette-ftp.user-code");

    private static final AttributeKey USER_DATA_ATTR = new AttributeKey(OdetteFtpSession.class, "odette-ftp.user-data");

    private static final AttributeKey VERSION_ATTR = new AttributeKey(OdetteFtpSession.class, "odette-ftp.version");

    private static final AttributeKey WINDOW_SIZE_ATTR = new AttributeKey(OdetteFtpSession.class,
            "odette-ftp.window-size");

    private static final AttributeKey V20_CHALLENGE_ATTR = new AttributeKey(OdetteFtpSession.class,
            "odette-ftp.v20.random-challenge");

    private static final AttributeKey V20_AUTHENTICATION_ATTR = new AttributeKey(OdetteFtpSession.class,
            "odette-ftp.v20.secure-authentication");

    private static final AttributeKey V20_CIPHER_SUITE_SELECTION_ATTR = new AttributeKey(OdetteFtpSession.class,
            "odette-ftp.v20.cipher-suite-selection");

    private static final AttributeKey V13_LONG_FILENAME_ATTR = new AttributeKey(OdetteFtpSession.class,
            "odette-ftp.v13.long-filenames");

    private static final AttributeKey LAST_COMMAND_RECEIVED = new AttributeKey(OdetteFtpSession.class,
            "odette-ftp.last-command-received");

    private static final AttributeKey LAST_COMMAND_SENT = new AttributeKey(OdetteFtpSession.class,
            "odette-ftp.last-command-sent");

    private static final AttributeKey OUTGOING_BYTES_TRANSFERED = new AttributeKey(OdetteFtpSession.class,
            "odette-ftp.outgoing.bytes-transfered");

    private static final AttributeKey INCOMING_BYTES_TRANSFERED = new AttributeKey(OdetteFtpSession.class,
            "odette-ftp.incoming.bytes-transfered");

    private EntityType entityType;

    private ChannelCallback channelCallback;

    private Map<AttributeKey, Object> attributes;

    public OdetteFtpSession(EntityType entityType) {
        super();
        this.entityType = entityType;
        this.attributes = new HashMap<AttributeKey, Object>();
        setState(ProtocolUtil.getInitialState(entityType));
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public ChannelCallback getChannelCallback() {
        return channelCallback;
    }

    public void setChannelCallback(ChannelCallback writerCallback) {
        this.channelCallback = writerCallback;
    }

    // OdetteFtpSession properties getters & setters
    // -------------------------------------------------------------------------

    public OdetteFtpVersion getVersion() {
        return getTypedAttribute(OdetteFtpVersion.class, VERSION_ATTR, DEFAULT_OFTP_VERSION);
    }

    public void setVersion(OdetteFtpVersion version) {
        setAttribute(VERSION_ATTR, version);
    }

    public long getTimeout() {
        return getTypedAttribute(Long.class, TIMEOUT_ATTR, DEFAULT_OFTP_SESSION_TIMEOUT);
    }

    public void setTimeout(long timeout) {
        setAttribute(TIMEOUT_ATTR, timeout);
    }

    public int getDataBufferSize() {
        return getTypedAttribute(Integer.class, SDEB_ATTR, DEFAULT_OFTP_DATA_EXCHANGE_BUFFER);
    }

    public void setDataBufferSize(int ssidsdeb) {
        setAttribute(SDEB_ATTR, ssidsdeb);
    }

    public String getResponseUser() {
        return getTypedAttribute(String.class, RESPONSE_USER_CODE_ATTR);
    }

    public String getResponseUserData() {
        return getTypedAttribute(String.class, RESPONSE_USER_DATA_ATTR);
    }

    public String getUserData() {
        return getTypedAttribute(String.class, USER_DATA_ATTR);
    }

    public String getUserCode() {
        return getTypedAttribute(String.class, USER_CODE_ATTR);
    }

    public int getWindowSize() {
        return getTypedAttribute(Integer.class, WINDOW_SIZE_ATTR, DEFAULT_OFTP_WINDOW_SIZE);
    }

    public int getIncomingCredits() {
        return getTypedAttribute(Integer.class, INCOMING_CREDITS_COUNTER_ATTR, 0);
    }

    public int getOutgoingCredits() {
        return getTypedAttribute(Integer.class, OUTGOING_CREDITS_COUNTER_ATTR, 0);
    }

    public long getOutgoingOffset() {
        return getTypedAttribute(Long.class, OUTGOING_TRANSFER_OFFSET_ATTR, 0L);
    }

    public long getIncomingOffset() {
        return getTypedAttribute(Long.class, INCOMING_TRANSFER_OFFSET_ATTR, 0L);
    }

    public boolean hasSpecialLogic() {
        return getTypedAttribute(Boolean.class, SPECIAL_LOGIC_ATTR, DEFAULT_OFTP_SPECIAL_LOGIC);
    }

    public boolean isCompressionSupported() {
        return getTypedAttribute(Boolean.class, BUFFER_COMPRESSION_ATTR, DEFAULT_OFTP_BUFFER_COMPRESSION);
    }

    public boolean isRestartSupported() {
        return getTypedAttribute(Boolean.class, RESTART_ATTR, DEFAULT_OFTP_RESTART);
    }

    public void setCompressionSupport(Boolean set) {
        setAttribute(BUFFER_COMPRESSION_ATTR, set);
    }

    public void setLongFilenames(Boolean set) {
        setAttribute(V13_LONG_FILENAME_ATTR, set);
    }

    public void setIncomingCredits(int c) {
        setAttribute(INCOMING_CREDITS_COUNTER_ATTR, c);
    }

    public void setIncomingOffset(long c) {
        setAttribute(INCOMING_TRANSFER_OFFSET_ATTR, c);
    }

    public void setOutgoingOffset(long c) {
        setAttribute(OUTGOING_TRANSFER_OFFSET_ATTR, c);
    }

    public void setOutgoingCredits(int c) {
        setAttribute(OUTGOING_CREDITS_COUNTER_ATTR, c);
    }

    public void setRestartSupport(boolean ssidrest) {
        setAttribute(RESTART_ATTR, ssidrest);
    }

    public void setUserData(String ssiduser) {
        setAttribute(USER_DATA_ATTR, ssiduser);
    }

    public void setSpecialLogic(Boolean ssidspec) {
        setAttribute(SPECIAL_LOGIC_ATTR, ssidspec);
    }

    public void setState(EntityState state) {
        setAttribute(STATE_ATTR, state);
    }

    public void setUserCode(String userCode) {
        setAttribute(USER_CODE_ATTR, userCode);
    }

    public void setWindowSize(int ssidcred) {
        setAttribute(WINDOW_SIZE_ATTR, ssidcred);
    }

    public String setResponseUser(String code) {
        setAttribute(RESPONSE_USER_CODE_ATTR, code);
        return code;
    }

    public String setResponseUserData(String data) {
        setAttribute(RESPONSE_USER_DATA_ATTR, data);
        return data;
    }

    public boolean useSecureAuthentication() {
        return getTypedAttribute(Boolean.class, V20_AUTHENTICATION_ATTR, DEFAULT_OFTP_V20_SECURE_AUTH);
    }

    public void setSecureAuthentication(boolean ssidauth) {
        setAttribute(V20_AUTHENTICATION_ATTR, ssidauth);
    }

    public byte[] getSecureAuthenticationChallenge() {
        return getTypedAttribute(byte[].class, V20_CHALLENGE_ATTR);
    }

    public void setSecureAuthenticationChallenge(byte[] plainChallenge) {
        setAttribute(V20_CHALLENGE_ATTR, plainChallenge);
    }

    public void setLastCommandReceived(CommandIdentifier identifier) {
        setAttribute(LAST_COMMAND_RECEIVED, identifier);
    }

    public CommandIdentifier getLastCommandReceived() {
        return getTypedAttribute(CommandIdentifier.class, LAST_COMMAND_RECEIVED);
    }

    public void setLastCommandSent(CommandIdentifier identifier) {
        setAttribute(LAST_COMMAND_SENT, identifier);
    }

    public CommandIdentifier getLastCommandSent() {
        return getTypedAttribute(CommandIdentifier.class, LAST_COMMAND_SENT);
    }

    public EntityState getState() {
        return getTypedAttribute(EntityState.class, STATE_ATTR);
    }

    public TransferMode getTransferMode() {
        return getTypedAttribute(TransferMode.class, MODE_ATTR);
    }

    public void setTransferMode(TransferMode mode) {
        setAttribute(MODE_ATTR, mode);
    }

    public long getOutgoingBytesTransfered() {
        return getTypedAttribute(Long.class, OUTGOING_BYTES_TRANSFERED);
    }

    public void setOutgoingBytesTransfered(long total) {
        setAttribute(OUTGOING_BYTES_TRANSFERED, total);
    }

    public long getIncomingBytesTransfered() {
        return getTypedAttribute(Long.class, INCOMING_BYTES_TRANSFERED);
    }

    public void setIncomingBytesTransfered(long total) {
        setAttribute(INCOMING_BYTES_TRANSFERED, total);
    }

    public void changeState() {
        if (getState() == SPEAKER) {
            setState(LISTENER);
        } else {
            setState(SPEAKER);
        }
    }

    public void setCipherSuiteSelection(CipherSuite selection) {
        setAttribute(V20_CIPHER_SUITE_SELECTION_ATTR, selection);
    }

    public CipherSuite getCipherSuiteSelection() {
        return getTypedAttribute(CipherSuite.class, V20_CIPHER_SUITE_SELECTION_ATTR, DEFAULT_OFTP2_CIPHER_SUITE);
    }

    // Implementation specific methods
    // -------------------------------------------------------------------------

    public boolean isResponder() {
        return (RESPONDER == getEntityType());
    }

    public boolean isInitiator() {
        return (INITIATOR == getEntityType());
    }

    public boolean isSendingSupported() {
        return (getTransferMode() != TransferMode.RECEIVER_ONLY);
    }

    public boolean isReceivingSupported() {
        return (getTransferMode() != TransferMode.SENDER_ONLY);
    }

    public void write(Object message) {
        write(message, null);
    }

    public void write(Object message, Runnable execOnComplete) {
        if (channelCallback == null) {
            throw new IllegalStateException("ChannelCallback is not set.");
        }
        channelCallback.write(message, execOnComplete);
    }

    public void close() {
        channelCallback.close();
    }

    public void closeImmediately() {
        channelCallback.closeImmediately();
    }

    public Object setAttribute(AttributeKey key, Object value) {
        return attributes.put(key, value);
    }

    public Object getAttribute(AttributeKey key) {
        return attributes.get(key);
    }

    public <T> T getTypedAttribute(Class<T> type, AttributeKey key) {
        return type.cast(getAttribute(key));
    }

    public <T> T getTypedAttribute(Class<T> type, AttributeKey key, T defaultValue) {
        T value = getTypedAttribute(type, key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

}
