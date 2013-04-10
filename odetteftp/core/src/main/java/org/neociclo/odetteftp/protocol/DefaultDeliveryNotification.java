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
package org.neociclo.odetteftp.protocol;

import java.util.Date;

import org.neociclo.odetteftp.util.ProtocolUtil;

/**
 * @author Rafael Marins
 */
public class DefaultDeliveryNotification implements DeliveryNotification {

    private static final long serialVersionUID = 1L;

    private String datasetName;
    private Date dateTime;
    private Short ticker;
    private String destination;
    private String originator;
    private String userData;

    private EndResponseType type;
    private String creator;
    private NegativeResponseReason reason;
    private String reasonText;

    public DefaultDeliveryNotification(EndResponseType type) {
        super();
        this.type = type;
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

    public EndResponseType getType() {
        return type;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public NegativeResponseReason getReason() {
        return reason;
    }

    public void setReason(NegativeResponseReason reason) {
        this.reason = reason;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((creator == null) ? 0 : creator.hashCode());
        result = prime * result + ((datasetName == null) ? 0 : datasetName.hashCode());
        result = prime * result + ((dateTime == null) ? 0 : dateTime.hashCode());
        result = prime * result + ((ticker == null) ? 0 : ticker.hashCode());
        result = prime * result + ((destination == null) ? 0 : destination.hashCode());
        result = prime * result + ((originator == null) ? 0 : originator.hashCode());
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
        result = prime * result + ((reasonText == null) ? 0 : reasonText.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((userData == null) ? 0 : userData.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof DefaultDeliveryNotification))
            return false;
        DefaultDeliveryNotification other = (DefaultDeliveryNotification) obj;
        if (creator == null) {
            if (other.creator != null)
                return false;
        } else if (!creator.equals(other.creator))
            return false;
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
        if (originator == null) {
            if (other.originator != null)
                return false;
        } else if (!originator.equals(other.originator))
            return false;
        if (reason == null) {
            if (other.reason != null)
                return false;
        } else if (!reason.equals(other.reason))
            return false;
        if (reasonText == null) {
            if (other.reasonText != null)
                return false;
        } else if (!reasonText.equals(other.reasonText))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
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
        sb.append("(").append(getType());
        sb.append(", dsn=").append(getDatasetName());
        sb.append(", dtm=").append(ProtocolUtil.formatDate("yyyy-MM-dd HH:mm:ss", getDateTime()));
        if (ticker != null) {
        	sb.append(", ticker=").append(ticker.toString());
        }
        sb.append(", orig=").append(getOriginator());
        sb.append(", dest=").append(getDestination()).append(")");
        return sb.toString();
    }

}
