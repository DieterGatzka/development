/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.03.05 at 01:29:11 PM CET 
//


package org.oscm.billingservice.business.model.billingresult;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Top level container element of the billing result file. It contains sections with the billing
 *         details for one customer.
 * 
 * <p>Java class for BillingDetailsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BillingDetailsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Period" type="{}PeriodType"/>
 *         &lt;element name="OrganizationDetails" type="{}OrganizationDetailsType"/>
 *         &lt;element name="Subscriptions" type="{}SubscriptionsType"/>
 *         &lt;element name="OverallCosts" type="{}OverallCostsType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="timezone" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="key" type="{http://www.w3.org/2001/XMLSchema}long" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BillingDetailsType", propOrder = {
    "period",
    "organizationDetails",
    "subscriptions",
    "overallCosts"
})
public class BillingDetailsType {

    @XmlElement(name = "Period", required = true)
    protected PeriodType period;
    @XmlElement(name = "OrganizationDetails", required = true)
    protected OrganizationDetailsType organizationDetails;
    @XmlElement(name = "Subscriptions", required = true)
    protected SubscriptionsType subscriptions;
    @XmlElement(name = "OverallCosts", required = true)
    protected OverallCostsType overallCosts;
    @XmlAttribute(required = true)
    protected String timezone;
    @XmlAttribute
    protected Long key;

    /**
     * Gets the value of the period property.
     * 
     * @return
     *     possible object is
     *     {@link PeriodType }
     *     
     */
    public PeriodType getPeriod() {
        return period;
    }

    /**
     * Sets the value of the period property.
     * 
     * @param value
     *     allowed object is
     *     {@link PeriodType }
     *     
     */
    public void setPeriod(PeriodType value) {
        this.period = value;
    }

    /**
     * Gets the value of the organizationDetails property.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationDetailsType }
     *     
     */
    public OrganizationDetailsType getOrganizationDetails() {
        return organizationDetails;
    }

    /**
     * Sets the value of the organizationDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationDetailsType }
     *     
     */
    public void setOrganizationDetails(OrganizationDetailsType value) {
        this.organizationDetails = value;
    }

    /**
     * Gets the value of the subscriptions property.
     * 
     * @return
     *     possible object is
     *     {@link SubscriptionsType }
     *     
     */
    public SubscriptionsType getSubscriptions() {
        return subscriptions;
    }

    /**
     * Sets the value of the subscriptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link SubscriptionsType }
     *     
     */
    public void setSubscriptions(SubscriptionsType value) {
        this.subscriptions = value;
    }

    /**
     * Gets the value of the overallCosts property.
     * 
     * @return
     *     possible object is
     *     {@link OverallCostsType }
     *     
     */
    public OverallCostsType getOverallCosts() {
        return overallCosts;
    }

    /**
     * Sets the value of the overallCosts property.
     * 
     * @param value
     *     allowed object is
     *     {@link OverallCostsType }
     *     
     */
    public void setOverallCosts(OverallCostsType value) {
        this.overallCosts = value;
    }

    /**
     * Gets the value of the timezone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Sets the value of the timezone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimezone(String value) {
        this.timezone = value;
    }

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setKey(Long value) {
        this.key = value;
    }

}
