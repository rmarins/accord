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
package org.neociclo.odetteftp.service;

import java.net.InetSocketAddress;

import javax.net.ssl.SSLContext;

import org.neociclo.odetteftp.TransportType;
import org.neociclo.odetteftp.oftplet.OftpletFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class X25MoreDataBitClient extends TcpClient {

	private static final TransportType X25_MBGW_TRANSPORT_TYPE = TransportType.X25_MBGW;

	public X25MoreDataBitClient(String host, OftpletFactory oftpletFactory) {
		super(host, oftpletFactory);
	}

	public X25MoreDataBitClient(String host, SSLContext sslContext, OftpletFactory oftpletFactory) {
		super(host, sslContext, oftpletFactory);
	}

	public X25MoreDataBitClient(String host, int port, OftpletFactory oftpletFactory) {
		super(host, port, oftpletFactory);
	}

	public X25MoreDataBitClient(String host, int port, SSLContext sslContext, OftpletFactory oftpletFactory) {
		super(host, port, sslContext, oftpletFactory);
	}

	public X25MoreDataBitClient(InetSocketAddress remoteAddress, OftpletFactory oftpletFactory) {
		super(remoteAddress, oftpletFactory);
	}

	public X25MoreDataBitClient(InetSocketAddress remoteAddress, SSLContext sslContext, OftpletFactory oftpletFactory) {
		super(remoteAddress, sslContext, oftpletFactory);
	}
	
	@Override
	protected TransportType getTransportType() {
		return X25_MBGW_TRANSPORT_TYPE;
	}

}
