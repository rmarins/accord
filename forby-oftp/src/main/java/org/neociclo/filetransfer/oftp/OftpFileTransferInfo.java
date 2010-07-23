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

import static org.neociclo.filetransfer.oftp.util.OftpConstants.*;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import org.neociclo.accord.filetransfer.DefaultFileTransferInfo;
import org.neociclo.accord.filetransfer.IFileTransferInfo;
import org.neociclo.odetteftp.protocol.IVirtualFile;
import org.neociclo.odetteftp.protocol.RecordFormat;
import org.neociclo.odetteftp.protocol.v20.CipherSuite;
import org.neociclo.odetteftp.protocol.v20.FileCompression;
import org.neociclo.odetteftp.protocol.v20.FileEnveloping;
import org.neociclo.odetteftp.protocol.v20.IEnvelopedVirtualFile;
import org.neociclo.odetteftp.protocol.v20.SecurityLevel;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class OftpFileTransferInfo extends DefaultFileTransferInfo implements IEnvelopedVirtualFile {

    private static final long serialVersionUID = 1L;

    public OftpFileTransferInfo(IVirtualFile cloned) {
        this((cloned == null || cloned.getFile() == null? null : new File(cloned.getFile().toURI())));

        setDatasetName(cloned.getDatasetName());
        setDateTime(cloned.getDateTime());
        setOriginator(cloned.getOriginator());
        setDestination(cloned.getDestination());
        setRecordFormat(cloned.getRecordFormat());
        setRecordSize(cloned.getRecordSize());
        setRestartOffset(cloned.getRestartOffset());
        setSize(cloned.getSize());
        setUserData(cloned.getUserData());

        if (cloned instanceof IEnvelopedVirtualFile) {
            IEnvelopedVirtualFile evf = (IEnvelopedVirtualFile) cloned;
            setCipherSuite(evf.getCipherSuite());
            setCompressionAlgorithm(evf.getCompressionAlgorithm());
            setFileDescription(evf.getFileDescription());
            setEnvelopingFormat(evf.getEnvelopingFormat());
            setOriginalFileSize(evf.getOriginalFileSize());
            setSecurityLevel(evf.getSecurityLevel());
            setSignedNotificationRequest(evf.isSignedNotificationRequest());
        }
    }

    public OftpFileTransferInfo(IFileTransferInfo cloned) {
        this((cloned == null || cloned.getFile() == null ? null : new File(cloned.getFile().toURI())));
        setDescription(cloned.getDescription());
        setMimeType(cloned.getMimeType());
        this.properties = new HashMap<String, Object>(cloned.getProperties());
    }

    public OftpFileTransferInfo(File localFileToSend) {
        super(localFileToSend);
        this.properties = new HashMap<String, Object>();
    }

    public RecordFormat getRecordFormat() {
        return (RecordFormat) properties.get(RECORD_FORMAT_PROP);
    }

    public void setRecordFormat(RecordFormat format) {
        properties.put(RECORD_FORMAT_PROP, format);
    }

    public int getRecordSize() {
        return (Integer) properties.get(RECORD_SIZE_PROP);
    }

    public void setRecordSize(int size) {
        properties.put(RECORD_SIZE_PROP, Integer.valueOf(size));
    }

    public long getRestartOffset() {
        return (Long) properties.get(RESTART_OFFSET_PROP);
    }

    public void setRestartOffset(long offset) {
        properties.put(RESTART_OFFSET_PROP, Long.valueOf(offset));
    }

    public long getSize() {
        return (Long) properties.get(FILE_SIZE_PROP);
    }

    public void setSize(long size) {
        properties.put(FILE_SIZE_PROP, Long.valueOf(size));
    }

    @Override
    public long getFileSize() {
        return getSize();
    }

    public void setFile(File payload) {
        this.file = payload;
    }

    public String getDatasetName() {
        return (String) properties.get(DATASET_NAME_PROP);
    }

    public void setDatasetName(String dsn) {
        properties.put(DATASET_NAME_PROP, dsn);
    }

    public Date getDateTime() {
        return (Date) properties.get(DATE_TIME_PROP);
    }

    public void setDateTime(Date dateTime) {
        properties.put(DATE_TIME_PROP, dateTime);
    }

    public String getDestination() {
        return (String) properties.get(DESTINATION_PROP);
    }

    public void setDestination(String oid) {
        properties.put(DESTINATION_PROP, oid);
    }

    public String getOriginator() {
        return (String) properties.get(ORIGINATOR_PROP);
    }

    public void setOriginator(String oid) {
        properties.put(ORIGINATOR_PROP, oid);
    }

    public String getUserData() {
        return (String) properties.get(USER_DATA_PROP);
    }

    public void setUserData(String data) {
        properties.put(USER_DATA_PROP, data);
    }

    public CipherSuite getCipherSuite() {
        return (CipherSuite) properties.get(CIPHER_SUITE_PROP);
    }

    public void setCipherSuite(CipherSuite cipher) {
        properties.put(CIPHER_SUITE_PROP, cipher);
    }

    public FileCompression getCompressionAlgorithm() {
        return (FileCompression) properties.get(COMPRESSION_ALGORITHM_PROP);
    }

    public void setCompressionAlgorithm(FileCompression compression) {
        properties.put(COMPRESSION_ALGORITHM_PROP, compression);
    }

    public FileEnveloping getEnvelopingFormat() {
        return (FileEnveloping) properties.get(ENVELOPING_FORMAT_PROP);
    }

    public void setEnvelopingFormat(FileEnveloping enveloping) {
        properties.put(ENVELOPING_FORMAT_PROP, enveloping);
    }

    public String getFileDescription() {
        return (String) properties.get(FILE_DESCRIPTION_PROP);
    }

    public void setFileDescription(String description) {
        properties.put(FILE_DESCRIPTION_PROP, description);
    }

    @Override
    public String getDescription() {
        return getFileDescription();
    }

    public long getOriginalFileSize() {
        return (Long) properties.get(ORIGINAL_FILE_SIZE_PROP);
    }

    public void setOriginalFileSize(long size) {
        properties.put(ORIGINAL_FILE_SIZE_PROP, Long.valueOf(size));
    }

    public SecurityLevel getSecurityLevel() {
        return (SecurityLevel) properties.get(SECURITY_LEVEL_PROP);
    }

    public void setSecurityLevel(SecurityLevel level) {
        properties.put(SECURITY_LEVEL_PROP, level);
    }

    public boolean isSignedNotificationRequest() {
        return (Boolean) properties.get(SIGNED_NOTIF_REQUEST_PROP);
    }

    public void setSignedNotificationRequest(boolean signed) {
        properties.put(SIGNED_NOTIF_REQUEST_PROP, Boolean.valueOf(signed));
    }

}
