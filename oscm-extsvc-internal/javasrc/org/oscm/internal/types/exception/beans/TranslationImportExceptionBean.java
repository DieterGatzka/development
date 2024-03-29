/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                        
 *       
 *  Creation Date: 2010-16-12                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.oscm.internal.types.exception.ImportException;

/**
 * Bean for JAX-WS exception serialization, specific for {@link ImportException}
 * .
 */
@XmlRootElement(name = "ImportExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class TranslationImportExceptionBean extends ApplicationExceptionBean {

    private static final long serialVersionUID = -8136601756267444526L;

    private String details;

    /**
     * Default constructor.
     */
    public TranslationImportExceptionBean() {
        super();
    }

    /**
     * Instantiates an <code>ImportExceptionBean</code> based on the specified
     * <code>ApplicationExceptionBean</code> and sets the given details.
     * 
     * @param sup
     *            the <code>ApplicationExceptionBean</code> to use as the base
     * @param details
     *            the detailed information for the exception
     */
    public TranslationImportExceptionBean(ApplicationExceptionBean sup, String details) {
        super(sup);
        setDetails(details);
    }

    /**
     * Returns the detailed information provided with the exception.
     * 
     * @return the detailed information
     */
    public String getDetails() {
        return details;
    }

    /**
     * Sets the detailed information for the exception.
     * 
     * @param details
     *            the detailed information
     */
    public void setDetails(String details) {
        this.details = details;
    }

}
