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

import org.neociclo.odetteftp.protocol.CommandExchangeBuffer;
import org.neociclo.odetteftp.protocol.DataExchangeBuffer;
import org.neociclo.odetteftp.protocol.EndSessionException;
import org.neociclo.odetteftp.protocol.EndSessionReason;

/**
 * @author Rafael Marins
 */
public interface ProtocolHandler {

    /**
     * Terminate current session by sending an End Session command with the
     * specified error and raise the exception for the local user agent.
     * 
     * @param session
     * @param err
     * @param msg
     * @throws EndSessionException
     *             a raising exception for the given End Session error to user
     *             agent.
     * @throws OdetteFtpException
     */
    void abnormalRelease(OdetteFtpSession session, EndSessionReason error, String errorText) throws OdetteFtpException;

    void abort(OdetteFtpSession session, EndSessionReason error, String errorText) throws OdetteFtpException;

    /**
     * Decrypts the challenge using the user private key and sends the decrypted
     * challenge back to the remote peer in the Authentication Response (AURP).
     * <p/>
     * The first authentication message must be sent by the Initiator.
     * 
     * <pre>
     * 1. Initiator -- SECD ------------&gt; Responder   Change Direction
     * 2.              &lt;------------ AUCH --             Challenge
     * 3.              -- AURP ------------&gt;             Response
     * 4.              &lt;------------ SECD --             Change Direction
     * 5.              -- AUCH ------------&gt;             Challenge
     * 6.              &lt;------------ AURP --             Response
     * </pre>
     * 
     * @param session
     * @param auch
     * @throws OdetteFtpException
     */
    void authenticationChallengeReceived(OdetteFtpSession session, CommandExchangeBuffer auch)
            throws OdetteFtpException;

    void authenticationResponseReceived(OdetteFtpSession session, CommandExchangeBuffer aurp)
            throws OdetteFtpException;

    void changeDirectionReceived(OdetteFtpSession session) throws OdetteFtpException;

    void dataBufferReceived(OdetteFtpSession session, DataExchangeBuffer data) throws OdetteFtpException;

    void endFileNegativeAnswerReceived(OdetteFtpSession session, CommandExchangeBuffer command)
            throws OdetteFtpException;

    void endFilePositiveAnswerReceived(OdetteFtpSession session, CommandExchangeBuffer command)
            throws OdetteFtpException;

    void endFileReceived(OdetteFtpSession session, CommandExchangeBuffer command) throws OdetteFtpException;

    void endSessionReceived(OdetteFtpSession session, CommandExchangeBuffer command) throws OdetteFtpException;

    void endToEndResponseReceived(OdetteFtpSession session, CommandExchangeBuffer command) throws OdetteFtpException;

    void negativeEndReponseReceived(OdetteFtpSession session, CommandExchangeBuffer command) throws OdetteFtpException;

    void protocolRelease(OdetteFtpSession session, EndSessionReason reason, String reasonText)
            throws OdetteFtpException;

    /**
     * The Initiator ODETTE-FTP entity receive the Start Session Ready Message
     * sent by the Responder immediately after the network connection has been
     * established, beginning the Start Session Phase.
     * <p>
     * Thus, the both negotiate session authentication and parameters exchanging
     * the SSID command.
     * <p>
     * <b>Protocol Sequence</b>
     * 
     * <pre>
     * 1. Initiator &lt;-------------SSRM -- Responder   Ready Message
     * 2.           -- SSID ------------&gt;             Identification
     * 3.           &lt;------------ SSID --             Identification
     * </pre>
     * 
     * @param session
     */
    void readyMessageReceived(OdetteFtpSession session) throws OdetteFtpException;

    /**
     * Ready To Receive Command (RTR) received in the SPEAKER state. Command
     * received to indicate that the End Response (EERP or NERP) sent has been
     * successfully received by the other peer.
     * <p>
     * In order to avoid congestion between two adjacent nodes caused by a
     * continuous flow of EERPs and NERPs, a Ready To Receive (RTR) command is
     * provided. The RTR acts as an EERP/NERP acknowledgement for flow control
     * but has no end-to-end significance.
     * <p>
     * <b>Protocol Sequence</b>
     * 
     * <pre>
     * 1. Speaker  -- EERP ------------&gt; Listener   End to End Response
     * 2.          &lt;------------- RTR --            Ready to Receive
     * 3.          -- EERP ------------&gt;            End to End Response
     * 4.          &lt;------------- RTR --            Ready to Receive
     * 5.          -- NERP ------------&gt;            Negative End Response
     * 6.          &lt;------------- RTR --            Ready to Receive
     * 7.          -- SFID ------------&gt;            Start File
     *                         or
     * 8.          -- CD --------------&gt;            Exchange the turn
     * </pre>
     * 
     * After sending an EERP or NERP, the Speaker must wait for an RTR before
     * sending any other commands. The only acceptable commands to follow are:
     * 
     * <pre>
     *      EERP
     *      NERP
     *      SFID or CD (if there are no more EERPs or NERPs to be sent)
     * </pre>
     * 
     * @param session
     * @throws OdetteFtpException
     */
    void readyToReceiveReceived(OdetteFtpSession session) throws OdetteFtpException;

    void securityChangeDirectionReceived(OdetteFtpSession session, CommandExchangeBuffer command)
            throws OdetteFtpException;

    /**
     * Indication that the network connection has been established - occur in
     * both entity states (SPEAKER or LISTENER). Allow handler to take the
     * proper protocol control when the session is connected.
     * <p>
     * Answer attended calls with a Start Session Ready Message command when
     * this ODETTE FTP entity is the Responder. Otherwise, do nothing else of
     * expecting same command from the peer.
     * <p>
     * The Start Session phase is entered immediately after the network
     * connection has been established. The ODETTE FTP entity that took the
     * initiative to establish the network connection becomes the Initiator.
     * It's peer becomes the Responder.
     * <p>
     * The first message must be sent by the Responder.
     * <p>
     * <b>Protocol Sequence</b>
     * 
     * <pre>
     * 1. Initiator &lt;-------------SSRM -- Responder   Ready Message
     * 2.           -- SSID ------------&gt;             Identification
     * 3.           &lt;------------ SSID --             Identification
     * </pre>
     * 
     * @param session
     * @throws OdetteFtpException
     */
    void sessionConnected(OdetteFtpSession session) throws OdetteFtpException;

    void setCreditReceived(OdetteFtpSession session, CommandExchangeBuffer command) throws OdetteFtpException;

    void startFileNegativeAnswerReceived(OdetteFtpSession session, CommandExchangeBuffer command)
            throws OdetteFtpException;

    void startFilePositiveAnswerReceived(OdetteFtpSession session, CommandExchangeBuffer command)
            throws OdetteFtpException;

    void startFileReceived(OdetteFtpSession session, CommandExchangeBuffer command) throws OdetteFtpException;

    /**
     * Negotiate the Start Session taking in account the right entity mode's
     * behavior (Initiator or Responder). The Responder use the received SSID
     * (in step 2) to authenticate the peer and determine the average session
     * parameters, meanwhile the Initiator handle the received SSID (in step 3)
     * to complete the Session Setup.
     * <p>
     * <b>Protocol Sequence</b>
     * 
     * <pre>
     * 1. Initiator &lt;-------------SSRM -- Responder   Ready Message
     * 2.           -- SSID ------------&gt;             Identification
     * 3.           &lt;------------ SSID --             Identification
     * </pre>
     * 
     * @param session
     * @param command
     * @throws OdetteFtpException
     */
    void startSessionReceived(OdetteFtpSession session, CommandExchangeBuffer command) throws OdetteFtpException;

    void afterStartSession(OdetteFtpSession session) throws OdetteFtpException;

}