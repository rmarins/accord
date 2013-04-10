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
package org.neociclo.odetteftp.camel.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.file.GenericFile;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.TransportType;
import org.neociclo.odetteftp.camel.OftpMessage;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.OdetteFtpObject;
import org.neociclo.odetteftp.protocol.VirtualFile;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;

/**
 * @author Rafael Marins
 */
@Converter
public class OftpConverter {

	@Converter
	public static OdetteFtpObject genericFileToOdetteFtpObject(GenericFile<?> file, Exchange exchange) throws IOException {

        Message in = exchange.getIn();

		DefaultVirtualFile vf = new DefaultVirtualFile((File) file.getFile());
		vf.setDatasetName(file.getFileNameOnly());
		vf.setDateTime(new Date(file.getLastModified()));

		String dest = in.getHeader(OftpMessage.OFTP_DESTINATION, String.class);
		if (dest != null) {
			vf.setDestination(dest);
		}

		return vf;
	}
	
    @Converter
    public static InputStream toInputStream(VirtualFile vf) {
        DefaultVirtualFile vFile = (DefaultVirtualFile) vf;
        try {
            return new FileInputStream(vFile.getFile());
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Converter
    public static OdetteFtpObject toOdetteFtpObject(File file) {
    	DefaultVirtualFile vf = new DefaultVirtualFile(file);
    	return vf;
    }

    @Converter
    public static File toFile(VirtualFile vf) {
        DefaultVirtualFile vFile = (DefaultVirtualFile) vf;
        return vFile.getFile();
    }

    @Converter
    public static TransferMode toTransferMode(String value) {
    	if (value.length() == 1) {
    		return TransferMode.parse(value);
    	}

    	return TransferMode.valueOf(value);
    }

    @Converter
    public static TransportType toTransportType(String value) {
    	TransportType result = null;
    	if ("TCP".equalsIgnoreCase(value) || "TCPIP".equalsIgnoreCase(value)) {
    		result = TransportType.TCPIP;
    	} else if ("MBGW".equalsIgnoreCase(value) || "X25-MBGW".equalsIgnoreCase(value) || "MoreDataBit".equalsIgnoreCase(value)) {
    		result = TransportType.X25_MBGW;
    	} else {
    		throw new IllegalArgumentException("Unrecognized Odette FTP transport: " + value);
    	}
    	return result;
    }

    @Converter
    public static CipherSuite toCipherSuite(String value) {
    	CipherSuite result = null;
    	
    	int code;
    	try {
    		code = Integer.parseInt(value);
    	} catch (NumberFormatException nfe) {
    		code = -1;
    	}

    	if (code == 0 || "noCipherSuiteSelection".equalsIgnoreCase(value)) {
    		result = CipherSuite.NO_CIPHER_SUITE_SELECTION;
    	} else if (code == 1 || "tripledesRsaSha1".equalsIgnoreCase(value)) {
    		result = CipherSuite.TRIPLEDES_RSA_SHA1;
    	} else if (code == 2 || "aes".equalsIgnoreCase(value)) {
    		result = CipherSuite.AES_RSA_SHA1;
    	} else {
    		throw new IllegalArgumentException("UnrecognizeOdetteFtpConvertercipher suite: " + value);
    	}

    	return result;
    }

	private OftpConverter() {
	}
}
