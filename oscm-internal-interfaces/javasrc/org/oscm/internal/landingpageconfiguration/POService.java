/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.internal.landingpageconfiguration;

import org.oscm.internal.base.BasePO;

public class POService extends BasePO {

    private static final long serialVersionUID = -1781005999877038205L;

    String serviceName;

    String providerName;

    String pictureUrl;

    String statusSymbol;

    public POService(long key, int version) {
        super(key, version);
    }

    public POService() {
        super(0, 0);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getStatusSymbol() {
        return statusSymbol;
    }

    public void setStatusSymbol(String statusSymbol) {
        this.statusSymbol = statusSymbol;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((pictureUrl == null) ? 0 : pictureUrl.hashCode());
        result = prime * result
                + ((providerName == null) ? 0 : providerName.hashCode());
        result = prime * result
                + ((serviceName == null) ? 0 : serviceName.hashCode());
        result = prime * result
                + ((statusSymbol == null) ? 0 : statusSymbol.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        POService other = (POService) obj;
        if (pictureUrl == null) {
            if (other.pictureUrl != null)
                return false;
        } else if (!pictureUrl.equals(other.pictureUrl))
            return false;
        if (providerName == null) {
            if (other.providerName != null)
                return false;
        } else if (!providerName.equals(other.providerName))
            return false;
        if (serviceName == null) {
            if (other.serviceName != null)
                return false;
        } else if (!serviceName.equals(other.serviceName))
            return false;
        if (statusSymbol == null) {
            if (other.statusSymbol != null)
                return false;
        } else if (!statusSymbol.equals(other.statusSymbol))
            return false;
        return true;
    }
}
