/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.billingservice.business.model.resellershare;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "organizationData", "period", "currency" })
@XmlRootElement(name = "ResellerRevenueShareResult")
public class ResellerRevenueShareResult {

    public static final String SCHEMA = "xmlns='http://oscm.org/xsd/billingservice/partnermodel' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://oscm.org/xsd/billingservice/partnermodel ResellerRevenueShareResult.xsd'";

    @XmlElement(name = "OrganizationData")
    protected OrganizationData organizationData;

    @XmlElement(name = "Period", required = true)
    protected Period period;

    @XmlElement(name = "Currency")
    protected List<Currency> currency;

    @XmlAttribute(name = "organizationKey", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger organizationKey;

    @XmlAttribute(name = "organizationId", required = true)
    protected String organizationId;

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period value) {
        this.period = value;
    }

    public List<Currency> getCurrency() {
        if (currency == null) {
            currency = new ArrayList<Currency>();
        }
        return this.currency;
    }

    public void addCurrency(Currency newCurrency) {
        getCurrency().add(newCurrency);
    }

    public Currency getCurrencyByCode(String currencyCode) {
        for (Currency currentCurrency : getCurrency()) {
            if (currentCurrency.getId().equals(currencyCode)) {
                return currentCurrency;
            }
        }
        return null;
    }
    
    public BigInteger getOrganizationKey() {
        return organizationKey;
    }

    public void setOrganizationKey(BigInteger value) {
        this.organizationKey = value;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String value) {
        this.organizationId = value;
    }

    public void calculateAllShares() {
        for (Currency currency : getCurrency()) {
            currency.calculate();
        }
    }

    public OrganizationData getOrganizationData() {
        return organizationData;
    }

    public void setOrganizationData(OrganizationData organizationData) {
        this.organizationData = organizationData;
    }
}
