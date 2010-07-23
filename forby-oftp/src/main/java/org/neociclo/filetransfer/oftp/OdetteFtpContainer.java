/**
 * Neociclo Accord, Open Source B2Bi Middleware
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
package org.neociclo.filetransfer.oftp;

import static org.neociclo.odetteftp.OdetteFtpVersion.OFTP_V20;
import static org.neociclo.odetteftp.TransferMode.BOTH;
import static org.neociclo.odetteftp.support.TransportType.TCPIP;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_OFTP_BUFFER_COMPRESSION;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_OFTP_DATA_EXCHANGE_BUFFER;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_OFTP_PORT;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_OFTP_RESTART;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_OFTP_SESSION_TIMEOUT;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_OFTP_WINDOW_SIZE;
import static org.neociclo.odetteftp.util.OdetteFtpConstants.DEFAULT_SECURE_OFTP_PORT;
import static org.neociclo.odetteftp.util.OftpUtil.isEmpty;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.neociclo.accord.filetransfer.ContainerConnectException;
import org.neociclo.accord.filetransfer.IConnectContext;
import org.neociclo.accord.filetransfer.ITransientConnectionContainerAdapter;
import org.neociclo.accord.filetransfer.spi.BaseInOutContainer;
import org.neociclo.odetteftp.service.Client;
import org.neociclo.odetteftp.service.TcpClient;
import org.neociclo.odetteftp.support.OftpletFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OdetteFtpContainer extends BaseInOutContainer implements ITransientConnectionContainerAdapter {

    private Object connectionLock = new Object();

    private boolean connected;

    public OdetteFtpContainer() {
        super();
    }

    public void execute(Serializable targetID, IConnectContext connectContext) throws ContainerConnectException {

        OdetteFtpClientConfiguration targetCfg;

        // evaluate proper target OFTP client configuration
        if (targetID == null) {
            throw new NullPointerException("targetID");
        } else if (targetID instanceof String) {
            try {
                targetCfg = new OdetteFtpClientConfiguration((String) targetID);
            } catch (URISyntaxException e) {
                throw new ContainerConnectException(targetID, "Bad OFTP connection string specified: " + targetID, e);
            }
        } else if (targetID instanceof URI) {
            try {
                targetCfg = new OdetteFtpClientConfiguration((URI) targetID);
            } catch (URISyntaxException e) {
                throw new ContainerConnectException(targetID, "Bad OFTP connection uri specified: " + targetID, e);
            }
        } else if (targetID instanceof OdetteFtpClientConfiguration) {
            targetCfg = (OdetteFtpClientConfiguration) targetID;
        } else {
            throw new IllegalArgumentException("Unsupported targetID type: " + targetID.getClass().getName());
        }

        checkMissingParameters(targetCfg);

        // set defaults for null parameters
        setDefaultsParameters(targetCfg);

        synchronized (connectionLock) {

            if (isConnected()) {
                throw new IllegalStateException("OdetteFtpContainer is already connected.");
            }

            OftpContext oc = new OftpContext(connectContext, targetCfg, outgoingQueue, retrieveQueue, incomingRequestListeners);
            OftpletFactory factory = new EventBasedOftpletFactory(oc);

            /* do connection */
            InetSocketAddress remoteAddr = new InetSocketAddress(targetCfg.getHost(), targetCfg.getPort());

            Client client = new TcpClient(factory, remoteAddr);

            Runnable onDisconnect = new Runnable() {
                public void run() {
                    setConnected(false);
                }
            };

            client.setDisconnectListener(onDisconnect);

            try {
                client.connect();
            } catch (Exception e) {
                throw new ContainerConnectException(targetID, "Odette FTP API Client connection failed.", e);
            }

            setConnected(true);
            
        }

    }

    private void checkMissingParameters(OdetteFtpClientConfiguration config) throws ContainerConnectException {

        if (isEmpty(config.getUserOid())) {
            throw new ContainerConnectException(config, "UserOID is mandatory.");
        }

        if (isEmpty(config.getHost())) {
            throw new ContainerConnectException(config, "Host is mandatory.");
        }

    }

    private void setDefaultsParameters(OdetteFtpClientConfiguration config) {

        // set default version to the newest one plus backward compatibility
        if (config.getVersion() == null) {
            config.setVersion(OFTP_V20);
        }

        boolean enabledForOftp2 = (config.getVersion() == OFTP_V20);

        // set default transport type TCPIP
        if (config.getTransport() == null) {
            config.setTransport(TCPIP);
        }

        // use BOTH transfer mode by default
        if (config.getMode() == null) {
            config.setMode(BOTH);
        }

        // set if SSL is enabled based on protocol version
        if (config.isSsl() == null) {
            config.setSsl(enabledForOftp2);
        }

        // set default port based upon SSL config
        if (config.getPort() == null || config.getPort() == -1) {
            config.setPort(config.isSsl() ? DEFAULT_SECURE_OFTP_PORT : DEFAULT_OFTP_PORT);
        }

        // set default connection timeout
        if (config.getTimeout() == null || config.getTimeout() <= 0) {
            config.setTimeout(DEFAULT_OFTP_SESSION_TIMEOUT);
        }

        // set defaults for protocol specific parameters

        if (config.getDebSize() == null || config.getDebSize() <= 0) {
            config.setDebSize(DEFAULT_OFTP_DATA_EXCHANGE_BUFFER);
        }

        if (config.getWindowSize() == null || config.getWindowSize() <= 0) {
            config.setWindowSize(DEFAULT_OFTP_WINDOW_SIZE);
        }

        if (config.isCompressionSupported() == null) {
            config.setCompressionSupported(DEFAULT_OFTP_BUFFER_COMPRESSION);
        }

        if (config.isRestartSupported() == null) {
            config.setRestartSupported(DEFAULT_OFTP_RESTART);
        }

        if (config.useSecureAuthentication() == null) {
            config.setUseSecureAuthentication(enabledForOftp2);
        }

    }

    public boolean isConnected() {
        return connected;
    }

    private void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public void dispose() {
        connectionLock = null;
    }

}
