/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                        
 *       
 *  Creation Date: 2009-09-22                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when trying to change a technical service to allow for only
 * one subscription, but an organization already has multiple subscriptions to
 * it.
 */
@WebFault(name = "TechnicalServiceMultiSubscriptions", targetNamespace = "http://oscm.org/xsd")
public class TechnicalServiceMultiSubscriptions extends
        SaaSApplicationException {

    private static final long serialVersionUID = -939263470561870070L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public TechnicalServiceMultiSubscriptions() {

    }

    /**
     * Constructs a new exception with the specified message parameters.
     * 
     * @param params
     *            the message parameters
     */
    public TechnicalServiceMultiSubscriptions(Object[] params) {
        super(params);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public TechnicalServiceMultiSubscriptions(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and bean for
     * JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     */
    public TechnicalServiceMultiSubscriptions(String message,
            ApplicationExceptionBean bean) {
        super(message, bean);
    }

    /**
     * Constructs a new exception with the specified detail message, cause, and
     * bean for JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     * @param cause
     *            the cause
     */
    public TechnicalServiceMultiSubscriptions(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

}
