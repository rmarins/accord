/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
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
package org.neociclo.odetteftp.protocol.v20;

import org.neociclo.odetteftp.protocol.DefaultVirtualFile;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class DefaultEnvelopedVirtualFile extends DefaultVirtualFile implements EnvelopedVirtualFile {

    private static final long serialVersionUID = 1L;

    private long originalFileSize;

    private SecurityLevel securityLevel;

    private CipherSuite cipherSuite;

    private FileCompression compressionAlgorithm;

    private FileEnveloping envelopingFormat;

    private boolean signedNotificationRequest;

    private String fileDescription;

    public DefaultEnvelopedVirtualFile() {
        super();
    }

    public long getOriginalFileSize() {
        return originalFileSize;
    }

    public void setOriginalFileSize(long originalFileSize) {
        this.originalFileSize = originalFileSize;
    }

    public SecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(SecurityLevel securityLevel) {
        this.securityLevel = securityLevel;
    }

    public CipherSuite getCipherSuite() {
        return cipherSuite;
    }

    public void setCipherSuite(CipherSuite ciphetSuite) {
        this.cipherSuite = ciphetSuite;
    }

    public FileCompression getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    public void setCompressionAlgorithm(FileCompression compressionAlgorithm) {
        this.compressionAlgorithm = compressionAlgorithm;
    }

    public FileEnveloping getEnvelopingFormat() {
        return envelopingFormat;
    }

    public void setEnvelopingFormat(FileEnveloping envelopingFormat) {
        this.envelopingFormat = envelopingFormat;
    }

    public boolean isSignedNotificationRequest() {
        return signedNotificationRequest;
    }

    public void setSignedNotificationRequest(boolean signed) {
        this.signedNotificationRequest = signed;
    }

    public String getFileDescription() {
        return fileDescription;
    }

    public void setFileDescription(String fileDescription) {
        this.fileDescription = fileDescription;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((cipherSuite == null) ? 0 : cipherSuite.hashCode());
        result = prime * result + ((compressionAlgorithm == null) ? 0 : compressionAlgorithm.hashCode());
        result = prime * result + ((envelopingFormat == null) ? 0 : envelopingFormat.hashCode());
        result = prime * result + ((fileDescription == null) ? 0 : fileDescription.hashCode());
        result = prime * result + (int) (originalFileSize ^ (originalFileSize >>> 32));
        result = prime * result + ((securityLevel == null) ? 0 : securityLevel.hashCode());
        result = prime * result + (signedNotificationRequest ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof DefaultEnvelopedVirtualFile))
            return false;
        DefaultEnvelopedVirtualFile other = (DefaultEnvelopedVirtualFile) obj;
        if (cipherSuite == null) {
            if (other.cipherSuite != null)
                return false;
        } else if (!cipherSuite.equals(other.cipherSuite))
            return false;
        if (compressionAlgorithm == null) {
            if (other.compressionAlgorithm != null)
                return false;
        } else if (!compressionAlgorithm.equals(other.compressionAlgorithm))
            return false;
        if (envelopingFormat == null) {
            if (other.envelopingFormat != null)
                return false;
        } else if (!envelopingFormat.equals(other.envelopingFormat))
            return false;
        if (fileDescription == null) {
            if (other.fileDescription != null)
                return false;
        } else if (!fileDescription.equals(other.fileDescription))
            return false;
        if (originalFileSize != other.originalFileSize)
            return false;
        if (securityLevel == null) {
            if (other.securityLevel != null)
                return false;
        } else if (!securityLevel.equals(other.securityLevel))
            return false;
        if (signedNotificationRequest != other.signedNotificationRequest)
            return false;
        return true;
    }

}
