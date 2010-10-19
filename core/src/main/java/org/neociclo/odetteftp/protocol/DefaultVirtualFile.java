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
package org.neociclo.odetteftp.protocol;

import java.io.File;
import java.util.Date;

import org.neociclo.odetteftp.util.ProtocolUtil;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class DefaultVirtualFile implements VirtualFile {

    private static final long serialVersionUID = 1L;

    private RecordFormat recordFormat;
    private int recordSize;
    private long restartOffset;
    private long size;
    private String datasetName;
    private Date dateTime;
    private Short ticker;
    private String destination;
    private String originator;
    private String userData;

    private File file;

    public RecordFormat getRecordFormat() {
        return recordFormat;
    }

    public void setRecordFormat(RecordFormat recordFormat) {
        this.recordFormat = recordFormat;
    }

    public int getRecordSize() {
        return recordSize;
    }

    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }

    public long getRestartOffset() {
        return restartOffset;
    }

    public void setRestartOffset(long restartOffset) {
        this.restartOffset = restartOffset;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public Date getDateTime() {
        return dateTime;
    }

	public Short getTicker() {
		return ticker;
	}

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

	public void setTicker(Short ticker) {
		this.ticker = ticker;
	}

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((datasetName == null) ? 0 : datasetName.hashCode());
        result = prime * result + ((dateTime == null) ? 0 : dateTime.hashCode());
        result = prime * result + ((ticker == null) ? 0 : ticker.hashCode());
        result = prime * result + ((destination == null) ? 0 : destination.hashCode());
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        result = prime * result + ((originator == null) ? 0 : originator.hashCode());
        result = prime * result + ((recordFormat == null) ? 0 : recordFormat.hashCode());
        result = prime * result + recordSize;
        result = prime * result + (int) (restartOffset ^ (restartOffset >>> 32));
        result = prime * result + (int) (size ^ (size >>> 32));
        result = prime * result + ((userData == null) ? 0 : userData.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof DefaultVirtualFile))
            return false;
        DefaultVirtualFile other = (DefaultVirtualFile) obj;
        if (datasetName == null) {
            if (other.datasetName != null)
                return false;
        } else if (!datasetName.equals(other.datasetName))
            return false;
        if (dateTime == null) {
            if (other.dateTime != null)
                return false;
        } else if (!dateTime.equals(other.dateTime))
            return false;
        if (ticker == null) {
            if (other.ticker != null)
                return false;
        } else if (!ticker.equals(other.ticker))
            return false;
        if (destination == null) {
            if (other.destination != null)
                return false;
        } else if (!destination.equals(other.destination))
            return false;
        if (file == null) {
            if (other.file != null)
                return false;
        } else if (!file.equals(other.file))
            return false;
        if (originator == null) {
            if (other.originator != null)
                return false;
        } else if (!originator.equals(other.originator))
            return false;
        if (recordFormat == null) {
            if (other.recordFormat != null)
                return false;
        } else if (!recordFormat.equals(other.recordFormat))
            return false;
        if (recordSize != other.recordSize)
            return false;
        if (restartOffset != other.restartOffset)
            return false;
        if (size != other.size)
            return false;
        if (userData == null) {
            if (other.userData != null)
                return false;
        } else if (!userData.equals(other.userData))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName());
        sb.append("(dsn=").append(getDatasetName());
        sb.append(", dtm=").append(ProtocolUtil.formatDate("yyyy-MM-dd HH:mm:ss", getDateTime()));
        if (ticker != null) {
        	sb.append(", ticker=").append(ticker.toString());
        }
        sb.append(", orig=").append(getOriginator());
        sb.append(", dest=").append(getDestination()).append(")");
        return sb.toString();
    }

}
