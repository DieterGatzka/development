/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.billingservice.business.model.brokershare;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.oscm.converter.PriceConverter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "supplier", "brokerRevenue" })
@XmlRootElement(name = "Currency")
public class Currency {

    @XmlElement(name = "Supplier", required = true)
    protected List<Supplier> supplier;

    @XmlElement(name = "BrokerRevenue", required = true)
    protected BrokerRevenue brokerRevenue = new BrokerRevenue();

    @XmlAttribute(name = "id", required = true)
    protected String id;

    public Currency() {
    }

    public Currency(String id) {
        setId(id);
    }

    public List<Supplier> getSupplier() {
        if (supplier == null) {
            supplier = new ArrayList<Supplier>();
        }
        return this.supplier;
    }

    public Supplier getSupplierByKey(long supplierKey) {
        for (Supplier supplier : getSupplier()) {
            if (supplier.getOrganizationData().getKey().longValue() == supplierKey) {
                return supplier;
            }
        }
        return null;

    }

    public void addSupplier(Supplier supplier) {
        getSupplier().add(supplier);
    }

    public BrokerRevenue getBrokerRevenue() {
        return brokerRevenue;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public void calculate() {
        BigDecimal currencyRevenue = BigDecimal.ZERO;
        BigDecimal totalCurrencyRevenue = BigDecimal.ZERO;
        for (Supplier supplier : getSupplier()) {
            supplier.calculate();
            currencyRevenue = currencyRevenue.add(supplier
                    .getBrokerRevenuePerSupplier().getAmount());
            totalCurrencyRevenue = totalCurrencyRevenue.add(supplier
                    .getBrokerRevenuePerSupplier().getTotalAmount());
        }
        currencyRevenue = currencyRevenue.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
        brokerRevenue.setAmount(currencyRevenue);
        totalCurrencyRevenue = totalCurrencyRevenue.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
        brokerRevenue.setTotalAmount(totalCurrencyRevenue);
    }
}
