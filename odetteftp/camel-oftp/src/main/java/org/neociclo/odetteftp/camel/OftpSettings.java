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
package org.neociclo.odetteftp.camel;

import java.io.File;
import java.net.URI;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.camel.RuntimeCamelException;
import org.apache.camel.util.ObjectHelper;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.TransportType;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;
import org.neociclo.odetteftp.support.OdetteFtpConfiguration;
import org.neociclo.odetteftp.util.OdetteFtpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpSettings implements Cloneable {

	public static final String CLIENT_PROTOCOL = "oftp";
	public static final String SERVER_PROTOCOL = "oftp-server";

	private static final transient Logger LOGGER = LoggerFactory.getLogger(OftpSettings.class);

	public static final int DEFAULT_POLLING_RETRY_COUNT = 2;

	private File workpath = new File(System.getProperty("java.io.tmpdir"));
	private FileRenameBean fileRenameBean = new FileRenameBean();

//	private boolean copyBeforeSend = true;
//	private boolean autoReplyDelivery = true;
//	private boolean routeFileRequest = false;
//	private boolean waitForEerp = false;
//	private long queueOfferDelay = 300;

	private String jaasRealm;

	private long maxFileSize;
	private boolean routeCommands = false;
	private boolean routeEvents = false;

	private String protocol;
	private TransportType transport = TransportType.TCPIP;

	private String usercode;
	private String password;

	// Address configuration
    private String host;
    private int port;

    // Secure connection configurations (SSL/TLS)
    private String passphrase;
    private File keyStoreFile;
    private File trustStoreFile;
    private String keyStoreFormat;
    private String securityProvider;
    private SSLContext sslContext;
    private boolean ssl = false;

    // OFTP session parameters 
	private String userData;
	private TransferMode transferMode = TransferMode.BOTH;
	private int debSize = 4096;
	private int windowSize = 64;
	private boolean compression = false;
	private boolean restart = false;
	private boolean specialLogic = false;
	private boolean secureAuth = false;
	private CipherSuite cipherSuite = CipherSuite.NO_CIPHER_SUITE_SELECTION;
	private OdetteFtpVersion version = OdetteFtpVersion.OFTP_V20;
	private OdetteFtpVersion downgradeVersion;
	private long timeout = 90000;

	// Threads/Executors configurations
    private int corePoolSize = 10;
    private int maxPoolSize = 100;

    private boolean overwrite = false;
	private boolean delete = true;

	public OftpSettings() {
		super();
	}

	/**
	 * <b>Supported Parameters</b>
	 * <ul>
	 * <li>none</li>
	 * </ul>
	 *
	 * @param uri the main URI part - <code>(oftp|oftp-server)[+(tcp|mbgw|isdn|xot)]://[user[:password]@address[:port]</code>
	 * @param parameters parsed URI parameters - <code>&lt;URI&gt;?&lt;parameters&gt;.
	 * @param component
	 * @throws Exception
	 */
	public void parseURI(URI uri, Map<String, Object> parameters, OftpComponent component) throws Exception {

		this.protocol = uri.getScheme();

		// UserInfo can contain both username and password as: user:pwd@serverhost
        // see: http://en.wikipedia.org/wiki/URI_scheme
        String oid = uri.getUserInfo();
        String pw = null;
        if (oid != null && oid.contains(":")) {
            pw = ObjectHelper.after(oid, ":");
            oid = ObjectHelper.before(oid, ":");
        }
        if (oid != null) {
            setUsercode(oid);
        }
        if (pw != null) {
            setPassword(pw);
        }

		setHost(uri.getHost());
		setPort(uri.getPort());

        sslContext = component.resolveAndRemoveReferenceParameter(parameters, "sslContext", SSLContext.class, null);
        passphrase = component.getAndRemoveParameter(parameters, "passphrase", String.class, null);
        keyStoreFormat = component.getAndRemoveParameter(parameters, "keyStoreFormat", String.class, "JKS");
        securityProvider = component.getAndRemoveParameter(parameters, "securityProvider", String.class, "SunX509");
        keyStoreFile = component.resolveAndRemoveReferenceParameter(parameters, "keyStoreFile", File.class, null);
        trustStoreFile = component.resolveAndRemoveReferenceParameter(parameters, "trustStoreFile", File.class, null);

        // then set parameters with the help of the camel context type converters
        component.setReferencesAndProperties(this, parameters);

        setDefaultsIfNotSet();

        ObjectHelper.notNull(workpath, "workpath");
        if (!workpath.exists()) {
			if (!workpath.mkdirs()) {
				throw new RuntimeCamelException("Impossible to create workpath: " + workpath);
			}
        }

	}

	public boolean isClient() {
		return CLIENT_PROTOCOL.equalsIgnoreCase(getProtocol());
	}

	public boolean isServer() {
		return SERVER_PROTOCOL.equalsIgnoreCase(getProtocol());
	}

	public String getAddressInfo() {
		StringBuffer addr = new StringBuffer(host);
		if (!isPortNotSet()) {
			addr.append(':').append(port);
		}
		if (transport != null) {
			addr.append(" (").append(transport.name()).append(')');
		}
		return addr.toString();
	}

	@Override
	public OftpSettings clone() throws CloneNotSupportedException {
		OftpSettings clone = (OftpSettings) super.clone();

		clone.setProtocol(getProtocol());
		clone.setTransport(getTransport());

		clone.setUsercode(getUsercode());
		clone.setPassword(getPassword());

		clone.setHost(getHost());
		clone.setPort(getPort());

		clone.setPassphrase(getPassphrase());
		clone.setKeyStoreFile(getKeyStoreFile());
		clone.setTrustStoreFile(getTrustStoreFile());
		clone.setKeyStoreFormat(getKeyStoreFormat());
		clone.setSecurityProvider(getSecurityProvider());
		clone.setSslContext(getSslContext());
		clone.setSsl(isSsl());

		clone.setUserData(getUserData());
		clone.setTransferMode(getTransferMode());
		clone.setDebSize(getDebSize());
		clone.setWindowSize(getWindowSize());
		clone.setCompression(isCompression());
		clone.setRestart(isRestart());
		clone.setSpecialLogic(isSpecialLogic());
		clone.setSecureAuth(isSecureAuth());
		clone.setCipherSuite(getCipherSuite());
		clone.setVersion(getVersion());
		clone.setTimeout(getTimeout());
		clone.setDowngradeVersion(getDowngradeVersion());
		clone.setMaxFileSize(getMaxFileSize());

		clone.setCorePoolSize(getCorePoolSize());
		clone.setMaxPoolSize(getMaxPoolSize());
		clone.setDelete(isDelete());
		clone.setOverwrite(isOverwrite());

		return clone;
	}

	boolean isPortNotSet() {
		return (getPort() <= 0);
	}

	private void setDefaultsIfNotSet() {
		if (isPortNotSet()) {
			int defaultPort = (isSsl() ? OdetteFtpConstants.DEFAULT_SECURE_OFTP_PORT : OdetteFtpConstants.DEFAULT_OFTP_PORT);
			LOGGER.debug("Using default Odette FTP port configuration: {} (ssl: {})", defaultPort, isSsl());
			setPort(defaultPort);
		}
	}

	public OdetteFtpConfiguration asOftpletConfiguration() {
		OdetteFtpConfiguration c = new OdetteFtpConfiguration();
		c.setUserData(getUserData());
		c.setTransferMode(getTransferMode());
		c.setDataExchangeBufferSize(getDebSize());
		c.setWindowSize(getWindowSize());
		c.setUseCompression(isCompression());
		c.setUseRestart(isRestart());
		c.setHasSpecialLogic(isSpecialLogic());
		c.setUseSecureAuthentication(isSecureAuth());
		c.setCipherSuiteSelection(getCipherSuite());
		c.setVersion(getVersion());
		c.setTimeout(getTimeout());
		return c;
	}

	// Getters and Setters
	// -------------------------------------------------------------------------

	public TransportType getTransport() {
		return transport;
	}

	public void setTransport(TransportType transport) {
		this.transport = transport;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public File getKeyStoreFile() {
		return keyStoreFile;
	}

	public void setKeyStoreFile(File keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}

	public File getTrustStoreFile() {
		return trustStoreFile;
	}

	public void setTrustStoreFile(File trustStoreFile) {
		this.trustStoreFile = trustStoreFile;
	}

	public String getKeyStoreFormat() {
		return keyStoreFormat;
	}

	public void setKeyStoreFormat(String keyStoreFormat) {
		this.keyStoreFormat = keyStoreFormat;
	}

	public String getSecurityProvider() {
		return securityProvider;
	}

	public void setSecurityProvider(String securityProvider) {
		this.securityProvider = securityProvider;
	}

	public SSLContext getSslContext() {
		return sslContext;
	}

	public void setSslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public String getUsercode() {
		return usercode;
	}

	public void setUsercode(String usercode) {
		this.usercode = usercode;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

	public TransferMode getTransferMode() {
		return transferMode;
	}

	public void setTransferMode(TransferMode transferMode) {
		this.transferMode = transferMode;
	}

	public int getDebSize() {
		return debSize;
	}

	public void setDebSize(int dataExchangeBufferSize) {
		this.debSize = dataExchangeBufferSize;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public boolean isCompression() {
		return compression;
	}

	public void setCompression(boolean bufferCompression) {
		this.compression = bufferCompression;
	}

	public boolean isRestart() {
		return restart;
	}

	public void setRestart(boolean restart) {
		this.restart = restart;
	}

	public boolean isSpecialLogic() {
		return specialLogic;
	}

	public void setSpecialLogic(boolean specialLogic) {
		this.specialLogic = specialLogic;
	}

	public boolean isSecureAuth() {
		return secureAuth;
	}

	public void setSecureAuth(boolean secureAuth) {
		this.secureAuth = secureAuth;
	}

	public CipherSuite getCipherSuite() {
		return cipherSuite;
	}

	public void setCipherSuite(CipherSuite cipherSuiteSel) {
		this.cipherSuite = cipherSuiteSel;
	}

	public OdetteFtpVersion getVersion() {
		return version;
	}

	public void setVersion(OdetteFtpVersion version) {
		this.version = version;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public File getWorkpath() {
		return workpath;
	}

	public void setWorkpath(File workpath) {
		this.workpath = workpath;
	}

	public FileRenameBean getFileRenameBean() {
		return fileRenameBean;
	}

	public void setFileRenameBean(FileRenameBean fileRenameBean) {
		this.fileRenameBean = fileRenameBean;
	}

	public boolean isRouteCommands() {
		return routeCommands;
	}

	public void setRouteCommands(boolean routeCommands) {
		this.routeCommands = routeCommands;
	}

	public boolean isRouteEvents() {
		return routeEvents;
	}

	public void setRouteEvents(boolean routeEvents) {
		this.routeEvents = routeEvents;
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public long getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public OdetteFtpVersion getDowngradeVersion() {
		return downgradeVersion;
	}

	public void setDowngradeVersion(OdetteFtpVersion downgradeVersion) {
		this.downgradeVersion = downgradeVersion;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public String getJaasRealm() {
		return jaasRealm;
	}

	public void setJaasRealm(String jaasRealm) {
		this.jaasRealm = jaasRealm;
	}

}
