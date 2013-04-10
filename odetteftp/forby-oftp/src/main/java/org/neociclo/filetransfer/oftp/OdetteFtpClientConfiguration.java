/**
 * Neociclo Accord, Open Source B2Bi Middleware
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
package org.neociclo.filetransfer.oftp;

import static java.lang.Boolean.*;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.support.TransportType;
import org.neociclo.odetteftp.util.OdetteFtpConstants;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OdetteFtpClientConfiguration implements Serializable {

    public static final String VERSION_PARAM = "version";

    public static final String MODE_PARAM = "mode";

    public static final String TRANSPORT_PARAM = "transport";

    public static final String CUSTOM_LONG_DSN_PARAM = "customLongDsn";

    public static final String TIMEOUT_PARAM = "timeout";

    public static final String DEB_SIZE_PARAM = "debSize";

    public static final String WINDOW_SIZE_PARAM = "windowSize";

    public static final String RESTART_PARAM = "restart";

    public static final String COMPRESSION_PARAM = "compression";

    public static final String SECURE_AUTH_PARAM = "secureAuth";

    public static final String SECURE_AUTH_KS_PARAM = "secureAuthKeystore";

    public static final String SECURE_AUTH_KS_PWD_PARAM = "secureAuthKeystorePassword";

    private static final long serialVersionUID = 1L;

    private static final String OFTP_SCHEME = "oftp";

    private static final String SECURE_OFTP_SCHEME = "oftps";

    private String host;

    private Integer port;

    private String userOid;

    private OdetteFtpVersion version;

    private TransferMode mode;

    private TransportType transport;

    private Boolean ssl;

    private Integer timeout;

    private Integer debSize;

    private Integer windowSize;

    private Boolean restartSupported;

    private Boolean compressionSupported;

    private Boolean useSecureAuthentication;

    private String secureAuthKeystore;

    private String secureAuthKeystorePassword;

    public OdetteFtpClientConfiguration() {
        super();
    }

    public OdetteFtpClientConfiguration(String connectionString) throws URISyntaxException {
        super();

        if (connectionString == null) {
            throw new NullPointerException("connectionString");
        }

        URI connectionUri = URI.create(connectionString);
        parse(connectionUri);
    }

    public OdetteFtpClientConfiguration(URI connectionUri) throws URISyntaxException {
        super();

        if (connectionUri == null) {
            throw new NullPointerException("connectionUri");
        }

        parse(connectionUri);
    }

    @Override
    public String toString() {
        try {
            return toURI().toString();
        } catch (URISyntaxException e) {
            return OdetteFtpClientConfiguration.class.getSimpleName() + " :: " + e.getMessage();
        }
    }

    public URI toURI() throws URISyntaxException {

        Map<Object, Object> queryMap = new LinkedHashMap<Object, Object>();

        if (getVersion() != null)
            queryMap.put(VERSION_PARAM, getVersion().name());

        if (getMode() != null)
            queryMap.put(MODE_PARAM, getMode().name());

        if (getTransport() != null)
            queryMap.put(TRANSPORT_PARAM, getTransport().name());

        if (getTimeout() != null)
            queryMap.put(TIMEOUT_PARAM, getTimeout().toString());

        if (getDebSize() != null)
            queryMap.put(DEB_SIZE_PARAM, getDebSize().toString());

        if (getWindowSize() != null)
            queryMap.put(WINDOW_SIZE_PARAM, getWindowSize().toString());

        if (isRestartSupported() != null)
            queryMap.put(RESTART_PARAM, isRestartSupported().toString());

        if (isCompressionSupported() != null)
            queryMap.put(COMPRESSION_PARAM, isCompressionSupported().toString());

        if (useSecureAuthentication() != null)
            queryMap.put(SECURE_AUTH_PARAM, useSecureAuthentication().toString());

        if (getSecureAuthKeystore() != null)
            queryMap.put(SECURE_AUTH_KS_PARAM, getSecureAuthKeystore());

        if (getSecureAuthKeystorePassword() != null)
            queryMap.put(SECURE_AUTH_KS_PWD_PARAM, getSecureAuthKeystorePassword());

        String query = createQueryString(queryMap);

        String scheme;
        int port;

        if (TRUE == isSsl()) {
            scheme = SECURE_OFTP_SCHEME;
            port = (getPort() == null ? OdetteFtpConstants.DEFAULT_SECURE_OFTP_PORT : getPort());
        } else {
            scheme = OFTP_SCHEME;
            port = (getPort() == null ? OdetteFtpConstants.DEFAULT_OFTP_PORT : getPort());
        }

        String userInfo = getUserOid();
        String host = getHost();

        return new URI(scheme, userInfo, host, port, null, query, null);
    }

    private void parse(URI uri) throws URISyntaxException {

        // set default port and security params based on the URI scheme
        String scheme = uri.getScheme();
        if (OFTP_SCHEME.equals(scheme)) {
            setSsl(false);
            setPort(OdetteFtpConstants.DEFAULT_OFTP_PORT);
        } else if (SECURE_OFTP_SCHEME.equals(scheme)) {
            setSsl(true);
            setPort(OdetteFtpConstants.DEFAULT_SECURE_OFTP_PORT);
        }
        // unknown scheme
        else {
            throw new IllegalArgumentException("Unknown protocol scheme: " + scheme);
        }

        // basic URI scheme info
        if (uri.getHost() != null)
            setHost(uri.getHost());

        if (uri.getPort() != -1)
            setPort(uri.getPort());

        setUserOid(uri.getUserInfo());

        // parse parameters specified in URI query info
        Map<String, Object> queryMap = parseQuery(uri.getQuery());

        if (queryMap.containsKey(VERSION_PARAM))
            setVersion(OdetteFtpVersion.valueOf((String) queryMap.get(VERSION_PARAM)));

        if (queryMap.containsKey(MODE_PARAM))
            setMode(TransferMode.valueOf((String) queryMap.get(MODE_PARAM)));

        if (queryMap.containsKey(TRANSPORT_PARAM))
            setTransport(TransportType.valueOf((String) queryMap.get(TRANSPORT_PARAM)));

        if (queryMap.containsKey(TIMEOUT_PARAM))
            setTimeout(Integer.valueOf((String) queryMap.get(TIMEOUT_PARAM)));

        if (queryMap.containsKey(DEB_SIZE_PARAM))
            setDebSize(Integer.valueOf((String) queryMap.get(DEB_SIZE_PARAM)));

        if (queryMap.containsKey(WINDOW_SIZE_PARAM))
            setWindowSize(Integer.valueOf((String) queryMap.get(WINDOW_SIZE_PARAM)));

        if (queryMap.containsKey(RESTART_PARAM))
            setRestartSupported(Boolean.valueOf((String) queryMap.get(RESTART_PARAM)));

        if (queryMap.containsKey(COMPRESSION_PARAM))
            setCompressionSupported(Boolean.valueOf((String) queryMap.get(COMPRESSION_PARAM)));

        if (queryMap.containsKey(SECURE_AUTH_PARAM))
            setUseSecureAuthentication(Boolean.valueOf((String) queryMap.get(SECURE_AUTH_PARAM)));

        if (queryMap.containsKey(SECURE_AUTH_KS_PARAM))
            setSecureAuthKeystore((String) queryMap.get(SECURE_AUTH_KS_PARAM));

        if (queryMap.containsKey(SECURE_AUTH_KS_PWD_PARAM))
            setSecureAuthKeystorePassword((String) queryMap.get(SECURE_AUTH_KS_PWD_PARAM));

    }

    // Getters and Setters
    // -------------------------------------------------------------------------

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUserOid() {
        return userOid;
    }

    public void setUserOid(String oid) {
        this.userOid = oid;
    }

    public OdetteFtpVersion getVersion() {
        return version;
    }

    public void setVersion(OdetteFtpVersion version) {
        this.version = version;
    }

    public TransferMode getMode() {
        return mode;
    }

    public void setMode(TransferMode mode) {
        this.mode = mode;
    }

    public TransportType getTransport() {
        return transport;
    }

    public void setTransport(TransportType transport) {
        this.transport = transport;
    }

    public Boolean isSsl() {
        return ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getDebSize() {
        return debSize;
    }

    public void setDebSize(Integer debSize) {
        this.debSize = debSize;
    }

    public Integer getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(Integer windowSize) {
        this.windowSize = windowSize;
    }

    public Boolean isRestartSupported() {
        return restartSupported;
    }

    public void setRestartSupported(Boolean restart) {
        this.restartSupported = restart;
    }

    public Boolean isCompressionSupported() {
        return compressionSupported;
    }

    public void setCompressionSupported(Boolean compression) {
        this.compressionSupported = compression;
    }

    public Boolean useSecureAuthentication() {
        return useSecureAuthentication;
    }

    public void setUseSecureAuthentication(Boolean secureAuthentication) {
        this.useSecureAuthentication = secureAuthentication;
    }

    public String getSecureAuthKeystore() {
        return secureAuthKeystore;
    }

    public void setSecureAuthKeystore(String keystoreFilePath) {
        this.secureAuthKeystore = keystoreFilePath;
    }

    public String getSecureAuthKeystorePassword() {
        return secureAuthKeystorePassword;
    }

    public void setSecureAuthKeystorePassword(String keystorePassword) {
        this.secureAuthKeystorePassword = keystorePassword;
    }
}
