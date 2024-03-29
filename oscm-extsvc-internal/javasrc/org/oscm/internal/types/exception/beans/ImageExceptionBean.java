/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                        
 *       
 *  Creation Date: 2011-07-25                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.oscm.internal.types.exception.ImageException;
import org.oscm.internal.types.exception.ImageException.Reason;

/**
 * Bean for JAX-WS exception serialization, specific for {@link ImageException}.
 */
@XmlRootElement(name = "ImageExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ImageExceptionBean extends ApplicationExceptionBean {

    private static final long serialVersionUID = -6987839662027441456L;

    private Reason reason;

    /**
     * Default constructor.
     */
    public ImageExceptionBean() {
        super();
    }

    /**
     * Instantiates an <code>ImageExceptionBean</code> based on the specified
     * <code>ApplicationExceptionBean</code> and sets the given reason.
     * 
     * @param sup
     *            the <code>ApplicationExceptionBean</code> to use as the base
     * @param reason
     *            the reason for the exception
     */
    public ImageExceptionBean(ApplicationExceptionBean sup, Reason reason) {
        super(sup);
        setReason(reason);
    }

    /**
     * Returns the reason for the exception.
     * 
     * @return the reason
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * Sets the reason for the exception.
     * 
     * @param reason
     *            the reason
     */
    public void setReason(Reason reason) {
        this.reason = reason;
    }

}
