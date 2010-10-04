/**
 *  Neociclo Accord - Open Source B2B Integration Suite
 *  Copyright (C) 2005-2008 Neociclo, http://www.neociclo.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package org.neociclo.accord.odetteftp.camel;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.camel.RuntimeCamelException;
import org.neociclo.odetteftp.TransferMode;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OdetteConfiguration implements Cloneable {

	public static final int DEFAULT_RETRY_COUNT = 2;

	private OdetteTransport transport;
	private File workpath = new File(System.getProperty("java.io.tmpdir"));
	private FileRenameBean fileRenameBean = new FileRenameBean();

	private OdetteProtocol protocol;
	private String host;
	private String oid;
	private String password;

	private int port;
	private int bufferSize = 4096;
	private int windowSize = 64;

	private boolean copyBeforeSend = true;
	private boolean autoResume = true;
	private boolean override = true;
	private boolean delete = true;
	private boolean autoReplyDelivery = true;
	private boolean routeFileRequest = false;
	private boolean waitForEerp = false;

	private long queueOfferDelay = 300;

	private long maxFileSize;

	private TransferMode transferMode;

	private SSLEngineFactory sslEngineFactory;

	private String passphrase;

	private File trustStoreFile;

	private File keyStoreFile;

	private String securityProvider;

	private String keyStoreFormat;

	public OdetteConfiguration() {
	}

	public OdetteConfiguration(URI uri) {
		this();
		configure(uri);
	}

	public OdetteProtocol getProtocol() {
		return protocol;
	}

	private OdetteProtocol validateProtocol(String protocol) {
		try {
			return OdetteProtocol.valueOf(protocol.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new RuntimeCamelException("Unsupported protocol: " + protocol);
		}
	}

	private void setProtocol(OdetteProtocol protocol) {
		this.protocol = protocol;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String username) {
		this.oid = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	protected void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	protected void setPort(int port) {
		if (port > -1) {
			this.port = port;
		}
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int dataExchangeBufferSize) {
		this.bufferSize = dataExchangeBufferSize;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public long getQueueOfferDelay() {
		return queueOfferDelay;
	}

	public void setQueueOfferDelay(long delay) {
		this.queueOfferDelay = delay;
	}

	/**
	 * <p>
	 * Configures this instance based on root information.
	 * </p>
	 * <p>
	 * <b>URI Schema:</b>
	 * <code>(oftp|oftps)[+(x25|isdn)]://[user[:pass]]@server[:port]</code>
	 * </p>
	 * 
	 */
	public void configure(URI uri) {
		String scheme = uri.getScheme();

		OdetteTransport transport = validateTransport(scheme);
		OdetteProtocol protocol = validateProtocol(scheme);

		setTransport(transport);
		setProtocol(protocol);
		setPort(protocol.defaultPort());

		setUserCredentials(uri.getUserInfo());
		setHost(uri.getHost());
		setPort(uri.getPort());
	}

	private void setTransport(OdetteTransport transport) {
		this.transport = transport;
	}

	protected OdetteTransport getTransport() {
		return transport;
	}

	private OdetteTransport validateTransport(String scheme) {
		int indexOfPlus = scheme.indexOf('+');
		if (indexOfPlus == -1) {
			return OdetteTransport.TCP;
		}

		try {
			return OdetteTransport.valueOf(scheme.substring(indexOfPlus).toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new RuntimeCamelException("Unsupported protocol: " + protocol);
		}
	}

	private void setUserCredentials(String userInfo) {
		boolean hasPassword = userInfo.indexOf(':') > 0; // can't be the first
															// character
		String u = userInfo, p = null;

		if (hasPassword) {
			u = userInfo.substring(0, userInfo.indexOf(':'));
			p = userInfo.substring(userInfo.indexOf(':') + 1);
		}

		setOid(u);
		setPassword(p);
	}

	@Override
	public OdetteConfiguration clone() throws CloneNotSupportedException {
		OdetteConfiguration clone = (OdetteConfiguration) super.clone();
		clone.setHost(host);
		clone.setPassword(password);
		clone.setProtocol(protocol);
		clone.setOid(oid);

		try {
			clone.setWorkpath(workpath);
		} catch (IOException e) {
		}

		clone.setTransport(transport);
		clone.setBufferSize(bufferSize);
		clone.setPort(port);
		clone.setWindowSize(windowSize);

		return clone;
	}

	public File getWorkpath() {
		return workpath;
	}

	public void setWorkpath(File workpath) throws IOException {
		this.workpath = workpath;

		if (workpath != null && !workpath.exists()) {
			if (!workpath.mkdirs()) {
				throw new RuntimeCamelException("Impossible to create workpath: " + workpath);
			}
		}
	}

	public FileRenameBean getFileRenameBean() {
		return fileRenameBean;
	}

	public void setFileRenameBean(FileRenameBean bean) {
		this.fileRenameBean = bean;
	}

	public boolean getAutoResume() {
		return this.autoResume;
	}

	public void setAutoResume(boolean autoResume) {
		this.autoResume = autoResume;
	}

	public boolean getOverride() {
		return override;
	}

	public void setOverride(boolean value) {
		this.override = value;
	}

	public long getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public boolean isCopyBeforeSend() {
		return copyBeforeSend;
	}

	public void setCopyBeforeSend(boolean copyBeforeSend) {
		this.copyBeforeSend = copyBeforeSend;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public boolean isAutoReplyDelivery() {
		return autoReplyDelivery;
	}

	public void setAutoReplyDelivery(boolean autoReplyDelivery) {
		this.autoReplyDelivery = autoReplyDelivery;
	}

	public void setRouteFileRequest(boolean routeFileRequest) {
		this.routeFileRequest = routeFileRequest;
	}

	public boolean isRouteFileRequest() {
		return this.routeFileRequest;
	}

	public void setWaitForEERP(boolean value) {
		this.waitForEerp = value;
	}

	public boolean isWaitForEERP() {
		return waitForEerp;
	}

	public void setTransferMode(TransferMode mode) {
		this.transferMode = mode;
	}

	public TransferMode getTransferMode() {
		return transferMode;
	}

	protected boolean isSsl() {
		return protocol == OdetteProtocol.OFTPS;
	}

	public SSLEngineFactory getSslEngineFactory() {
		return sslEngineFactory;
	}

	public void setSSLEngineFactory(SSLEngineFactory engineFactory) {
		this.sslEngineFactory = engineFactory;
	}

	public String getKeyStoreFormat() {
		return keyStoreFormat;
	}

	public void setSslEngineFactory(SSLEngineFactory sslEngineFactory) {
		this.sslEngineFactory = sslEngineFactory;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public void setTrustStoreFile(File trustStoreFile) {
		this.trustStoreFile = trustStoreFile;
	}

	public void setKeyStoreFile(File keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}

	public void setSecurityProvider(String securityProvider) {
		this.securityProvider = securityProvider;
	}

	public void setKeyStoreFormat(String keyStoreFormat) {
		this.keyStoreFormat = keyStoreFormat;
	}

	public String getSecurityProvider() {
		return securityProvider;
	}

	public File getKeyStoreFile() {
		return keyStoreFile;
	}

	public File getTrustStoreFile() {
		return trustStoreFile;
	}

	public String getPassphrase() {
		return passphrase;
	}
}
