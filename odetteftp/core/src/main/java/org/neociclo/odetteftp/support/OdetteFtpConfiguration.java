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
package org.neociclo.odetteftp.support;

import java.io.Serializable;

import org.neociclo.odetteftp.OdetteFtpSession;
import org.neociclo.odetteftp.OdetteFtpVersion;
import org.neociclo.odetteftp.TransferMode;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;

/**
 * @author Rafael Marins
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
	private Long timeout;

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

	public void setUseCompression(Boolean useCompression) {
		this.useCompression = useCompression;
	}

	public Boolean getUseRestart() {
		return useRestart;
	}

	public void setUseRestart(Boolean useRestart) {
		this.useRestart = useRestart;
	}

	public Boolean getHasSpecialLogic() {
		return hasSpecialLogic;
	}

	public void setHasSpecialLogic(Boolean hasSpecialLogic) {
		this.hasSpecialLogic = hasSpecialLogic;
	}

	public Boolean useSecureAuthentication() {
		return useSecureAuthentication;
	}
	public Boolean getUseSecureAuthentication() {
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

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
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

        if (timeout != null) {
        	s.setTimeout(timeout);
        }
        
    }

}
