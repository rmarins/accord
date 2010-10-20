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
package org.neociclo.odetteftp.support;

import java.io.Serializable;

import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OdetteFtpConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	private String userData;
	private TransferMode transferMode;
	private Integer dataExchangeBufferSize;
	private Integer windowSize;
	private Boolean useCompression;
	private Boolean useRestart;
	private Boolean hasSpecialLogic;
	private Boolean useSecureAuthentication;
	private CipherSuite cipherSuiteSelection;
	private OdetteFtpVersion version;

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

	public Integer getDataExchangeBufferSize() {
		return dataExchangeBufferSize;
	}

	public void setDataExchangeBufferSize(Integer dataExchangeBufferSize) {
		this.dataExchangeBufferSize = dataExchangeBufferSize;
	}

	public Integer getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(Integer windowSize) {
		this.windowSize = windowSize;
	}

	public Boolean getUseCompression() {
		return useCompression;
	}

	public void useCompression(Boolean useCompression) {
		this.useCompression = useCompression;
	}

	public Boolean getUseRestart() {
		return useRestart;
	}

	public void useRestart(Boolean useRestart) {
		this.useRestart = useRestart;
	}

	public Boolean getHasSpecialLogic() {
		return hasSpecialLogic;
	}

	public void hasSpecialLogic(Boolean hasSpecialLogic) {
		this.hasSpecialLogic = hasSpecialLogic;
	}

	public Boolean useSecureAuthentication() {
		return useSecureAuthentication;
	}

	public void setUseSecureAuthentication(Boolean useSecureAuthentication) {
		this.useSecureAuthentication = useSecureAuthentication;
	}

	public CipherSuite getCipherSuiteSelection() {
		return cipherSuiteSelection;
	}

	public void setCipherSuiteSelection(CipherSuite cipherSuiteSelection) {
		this.cipherSuiteSelection = cipherSuiteSelection;
	}

	public OdetteFtpVersion getVersion() {
		return version;
	}

	public void setVersion(OdetteFtpVersion version) {
		this.version = version;
	}

    public void setup(OdetteFtpSession s) {

    	if (userData != null) {
    		s.setUserData(userData);
    	}

        if (version == null) {
        	if (s.getVersion() == null) {
        		s.setVersion(OdetteFtpVersion.OFTP_V20);
        	}
        } else {
            s.setVersion(version);
        }

        if (transferMode == null) {
        	if (s.getTransferMode() == null) {
        		s.setTransferMode(TransferMode.SENDER_ONLY);
        	}
        } else {
            s.setTransferMode(transferMode);
        }

        if (dataExchangeBufferSize != null) {
            s.setDataBufferSize(dataExchangeBufferSize);
        }

        if (windowSize != null) {
            s.setWindowSize(windowSize);
        }

        if (useRestart != null) {
            s.setRestartSupport(useRestart);
        }

        if (useCompression != null) {
            s.setCompressionSupport(useCompression);
        }

        if (hasSpecialLogic != null) {
            s.setSpecialLogic(hasSpecialLogic);
        }

        if (useSecureAuthentication != null) {
            s.setSecureAuthentication(useSecureAuthentication);
        }

        if (cipherSuiteSelection != null) {
            s.setCipherSuiteSelection(cipherSuiteSelection);
        }
        
    }

}
