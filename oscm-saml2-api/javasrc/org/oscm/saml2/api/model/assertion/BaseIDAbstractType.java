/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.05.22 at 12:50:01 PM CEST 
//

package org.oscm.saml2.api.model.assertion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for BaseIDAbstractType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="BaseIDAbstractType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{urn:oasis:names:tc:SAML:2.0:assertion}IDNameQualifiers"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BaseIDAbstractType")
public abstract class BaseIDAbstractType {

    @XmlAttribute(name = "NameQualifier")
    protected String nameQualifier;
    @XmlAttribute(name = "SPNameQualifier")
    protected String spNameQualifier;

    /**
     * Gets the value of the nameQualifier property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getNameQualifier() {
        return nameQualifier;
    }

    /**
     * Sets the value of the nameQualifier property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setNameQualifier(String value) {
        this.nameQualifier = value;
    }

    /**
     * Gets the value of the spNameQualifier property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSPNameQualifier() {
        return spNameQualifier;
    }

    /**
     * Sets the value of the spNameQualifier property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setSPNameQualifier(String value) {
        this.spNameQualifier = value;
    }

}
