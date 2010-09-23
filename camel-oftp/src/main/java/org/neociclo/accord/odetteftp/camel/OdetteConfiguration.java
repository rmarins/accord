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

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OdetteConfiguration implements Cloneable {

	public static final int DEFAULT_RETRY_COUNT = 2;

	private OdetteTransport transport;
	private File workpath = new File(System.getProperty("java.io.tmpdir"));;
	private FileRenameBean fileRenameBean = new FileRenameBean();

	private String protocol;
	private String host;
	private String oid;
	private String password;

	private int port;
	private int bufferSize = 4096;
	private int windowSize = 64;
	private int maxRetry = 0;
	private int eerpTimeout = 300;

	private boolean longFilename = false;
	private boolean useFixedDelay;
	private boolean copyBeforeSend = true;
	private boolean autoResume = true;
	private boolean override = true;
	private boolean delete = true;
	private boolean autoReplyDelivery = true;
	private boolean routeFileRequest = false;

	private long timeout = 90000;
	private long initialDelay = 10;
	private long delay = 300;
	private long maxFileSize;

	public OdetteConfiguration() {
	}

	public OdetteConfiguration(URI uri) {
		this();
		configure(uri);
	}

	public int getMaxRetry() {
		return maxRetry;
	}

	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}

	public String getProtocol() {
		return protocol;
	}

	private OdetteProtocol validateProtocol(String protocol) {
		try {
			return OdetteProtocol.valueOf(protocol.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new RuntimeCamelException("Unsupported protocol: " + protocol);
		}
	}

	protected void setProtocol(String protocol) {
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

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getInitialDelay() {
		return initialDelay;
	}

	public void setInitialDelay(long initialDelay) {
		this.initialDelay = initialDelay;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public void setUseFixedDelay(boolean useFixedDelay) {
		this.useFixedDelay = useFixedDelay;
	}

	public boolean isLongFilename() {
		return longFilename;
	}

	public void setLongFilename(boolean longFilename) {
		this.longFilename = longFilename;
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
		setProtocol(protocol.name().toLowerCase());
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
		clone.setDelay(delay);
		clone.setInitialDelay(initialDelay);
		clone.setMaxRetry(maxRetry);
		clone.setPort(port);
		clone.setTimeout(timeout);
		clone.setWindowSize(windowSize);
		clone.setUseFixedDelay(useFixedDelay);

		return clone;
	}

	public File getWorkpath() {
		return workpath;
	}

	public void setWorkpath(File workpath) throws IOException {
		this.workpath = workpath;

		if (workpath != null && !workpath.exists()) {
			workpath.mkdir();
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

	public void setEerpTimeout(int eerpTimeout) {
		this.eerpTimeout = eerpTimeout;
	}

	public int getEerpTimeout() {
		return eerpTimeout;
	}

}
