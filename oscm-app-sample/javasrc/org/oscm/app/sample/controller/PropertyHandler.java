/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                        
 *       
 *  Sample controller implementation for the 
 *  Asynchronous Provisioning Platform (APP)
 *       
 *  Creation Date: 2012-09-06                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.sample.controller;

import org.oscm.app.v1_0.data.PasswordAuthentication;
import org.oscm.app.v1_0.data.ProvisioningSettings;

/**
 * Helper class to handle service parameters and controller configuration
 * settings. The implementation shows how the settings can be managed in a
 * centralized way.
 * <p>
 * The underlying <code>ProvisioningSettings</code> object of APP provides all
 * the specified service parameters and controller configuration settings
 * (key/value pairs). The settings are stored in the APP database and therefore
 * available even after restarting the application server.
 */
public class PropertyHandler {
    // Holds the provided settings
    private ProvisioningSettings settings;

    /**
     * A text message which is sent in emails during the sample provisioning
     * process. The message is specified as a service parameter in the technical
     * service definition.
     */
    public static final String TECPARAM_MESSAGETEXT = "PARAM_MESSAGETEXT";

    /**
     * The recipient to whom notification emails are sent. The recipient is
     * specified as a service parameter in the technical service definition.
     */
    public static final String TECPARAM_EMAIL = "PARAM_EMAIL";

    /**
     * The internal status of a provisioning operation as set by the controller
     * or the status dispatcher.
     */
    public static final String STATUS = "STATUS";

    /**
     * The key of the property for specifying the user key of the technology
     * manager to be used for service calls to Catalog Manager. The user must be
     * a member of the technology provider organization for which the service
     * controller has been registered.
     */
    public static final String BSS_USER = "APP_BSS_USER";

    /**
     * The key of the property for specifying the password of the user to be
     * used for service calls to Catalog Manager.
     */
    public static final String BSS_USER_PWD = "APP_BSS_USER_PWD";

    /**
     * Default constructor.
     * 
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * 
     */
    public PropertyHandler(ProvisioningSettings settings) {
        this.settings = settings;
    }

    /**
     * Returns the text message which is sent in emails during the sample
     * provisioning process.
     * 
     * @return the message as a string
     */
    public String getMessage() {
        return settings.getParameters().get(TECPARAM_MESSAGETEXT);
    }

    /**
     * Returns the recipient to whom notification emails are sent.
     * 
     * @return the email address as a string
     */
    public String getEMail() {
        return settings.getParameters().get(TECPARAM_EMAIL);
    }

    /**
     * Returns the internal status of the current provisioning operation as set
     * by the controller or the status dispatcher.
     * 
     * @return the current status
     */
    public Status getState() {
        String status = settings.getParameters().get(STATUS);
        return (status != null) ? Status.valueOf(status) : Status.FAILED;
    }

    /**
     * Changes the internal status for the current provisioning operation.
     * 
     * @param newState
     *            the new status to set
     */
    public void setState(Status newState) {
        settings.getParameters().put(STATUS, newState.toString());
    }

    /**
     * Returns the current service parameters and controller configuration
     * settings.
     * 
     * @return a <code>ProvisioningSettings</code> object specifying the
     *         parameters and settings
     */
    public ProvisioningSettings getSettings() {
        return settings;
    }

    /**
     * Returns the instance or controller specific technology manager
     * authentication.
     */
    public PasswordAuthentication getTPAuthentication() {
        return settings.getAuthentication();
    }

}
