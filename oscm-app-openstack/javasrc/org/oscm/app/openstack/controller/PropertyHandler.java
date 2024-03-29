/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                        
 *       
 *  OpenStack controller implementation for the 
 *  Asynchronous Provisioning Platform (APP)
 *       
 *  Creation Date: 2013-11-29                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.openstack.controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oscm.app.openstack.data.FlowState;
import org.oscm.app.openstack.exceptions.HeatException;
import org.oscm.app.v1_0.BSSWebServiceFactory;
import org.oscm.app.v1_0.data.PasswordAuthentication;
import org.oscm.app.v1_0.data.ProvisioningSettings;

/**
 * Helper class to handle service parameters and controller configuration
 * settings.
 * <p>
 * The underlying <code>ProvisioningSettings</code> object of APP provides all
 * the specified service parameters and controller configuration settings
 * (key/value pairs). The settings are stored in the APP database and therefore
 * available even after restarting the application server.
 */
public class PropertyHandler {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PropertyHandler.class);

    private final ProvisioningSettings settings;

    public static final String STACK_NAME = "STACK_NAME";
    public static final String STACK_ID = "STACK_ID";

    // Name (not id) of the tenant/project (if omitted, it is taken from
    // controller configuration)
    public static final String TENANT_NAME = "TENANT_NAME";

    // URL of Heat template
    public static final String TEMPLATE_NAME = "TEMPLATE_NAME";

    // AccessInfo creation pattern
    public static final String ACCESS_INFO_PATTERN = "ACCESS_INFO_PATTERN";

    // Base URL of Heat templates
    public static final String TEMPLATE_BASE_URL = "TEMPLATE_BASE_URL";

    // Prefix for template parameters
    public static final String TEMPLATE_PARAMETER_PREFIX = "TP_";

    public static final String KEYSTONE_API_URL = "KEYSTONE_API_URL";
    public static final String API_USER_NAME = "API_USER_NAME";
    public static final String API_USER_PWD = "API_USER_PWD";

    /**
     * Defines whether manual steps are required before activation the
     * deployment for the customer.
     */
    public static final String MAIL_FOR_COMPLETION = "MAIL_FOR_COMPLETION";

    /**
     * The internal status of a provisioning operation as set by the controller
     * or the status dispatcher.
     */
    public static final String STATUS = "STATUS";

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
     * Returns the internal state of the current provisioning operation as set
     * by the controller or the dispatcher.
     * 
     * @return the current status
     */
    public FlowState getState() {
        String status = settings.getParameters().get(STATUS);
        return (status != null) ? FlowState.valueOf(status) : FlowState.FAILED;
    }

    /**
     * Changes the internal state for the current provisioning operation.
     * 
     * @param newState
     *            the new state to set
     */
    public void setState(FlowState newState) {
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
     * Returns the name of the stack (=instance identifier).
     * 
     * @return the name of the stack
     */
    public String getStackName() {
        return getValidatedProperty(settings.getParameters(), STACK_NAME);
    }

    public void setStackName(String stackName) {
        settings.getParameters().put(STACK_NAME, stackName);
    }

    /**
     * Returns the heat specific id of the stack.
     * 
     * @return the id of the stack
     */
    public String getStackId() {
        return settings.getParameters().get(STACK_ID);
    }

    public void setStackId(String stackId) {
        settings.getParameters().put(STACK_ID, stackId);
    }

    /**
     * Returns the access information pattern used to created the instance
     * access information using the output parameters of the created stack.
     * 
     * @return the access information pattern
     */
    public String getAccessInfoPattern() {
        return getValidatedProperty(settings.getParameters(),
                ACCESS_INFO_PATTERN);
    }

    /**
     * Returns the URL of the template to be used for provisioning.
     * 
     * @return the template URL
     */
    public String getTemplateUrl() throws HeatException {

        try {
            String url = getValidatedProperty(settings.getParameters(),
                    TEMPLATE_NAME);
            String baseUrl = getValidatedProperty(settings.getConfigSettings(),
                    TEMPLATE_BASE_URL);
            return new URL(new URL(baseUrl), url).toExternalForm();
        } catch (MalformedURLException e) {
            throw new HeatException("Cannot generate template URL: "
                    + e.getMessage());
        }
    }

    /**
     * Returns the tenant name that defines the context for the provisioning. It
     * can either be defined within the controller settings of as instance
     * parameter. When present, the service parameter is preferred.
     * 
     * @return the tenant name
     */
    public String getTenantName() {
        String tenant = settings.getParameters().get(TENANT_NAME);
        if (tenant == null || tenant.trim().length() == 0) {
            tenant = getValidatedProperty(settings.getConfigSettings(),
                    TENANT_NAME);
        }
        return tenant;
    }

    public JSONObject getTemplateParameters() {
        JSONObject parameters = new JSONObject();
        Set<String> keySet = settings.getParameters().keySet();
        for (String key : keySet) {
            if (key.startsWith(TEMPLATE_PARAMETER_PREFIX)) {
                try {
                    parameters.put(
                            key.substring(TEMPLATE_PARAMETER_PREFIX.length()),
                            settings.getParameters().get(key));
                } catch (JSONException e) {
                    // should not happen with Strings
                    throw new RuntimeException(
                            "JSON error when collection template parameters", e);
                }
            }
        }
        return parameters;
    }

    /**
     * Reads the requested property from the available parameters. If no value
     * can be found, a RuntimeException will be thrown.
     * 
     * @param sourceProps
     *            The property object to take the settings from
     * @param key
     *            The key to retrieve the setting for
     * @return the parameter value corresponding to the provided key
     */
    private String getValidatedProperty(Map<String, String> sourceProps,
            String key) {
        String value = sourceProps.get(key);
        if (value == null) {
            String message = String.format("No value set for property '%s'",
                    key);
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
        return value;
    }

    /**
     * Return the URL of the Keystone API which acts as entry point to all other
     * API endpoints.
     * 
     * @return the Keystone URL
     */
    public String getKeystoneUrl() {
        return getValidatedProperty(settings.getConfigSettings(),
                KEYSTONE_API_URL);
    }

    /**
     * Returns the configured password for API usage.
     * 
     * @return the password
     */
    public String getPassword() {
        return getValidatedProperty(settings.getConfigSettings(), API_USER_PWD);
    }

    /**
     * Returns the configured user name for API usage.
     * 
     * @return the user name
     */
    public String getUserName() {
        return getValidatedProperty(settings.getConfigSettings(), API_USER_NAME);
    }

    /**
     * Returns the mail address to be used for completion events (provisioned,
     * deleted). If not set, no events are required.
     * 
     * @return the mail address or <code>null</code> if no events are required
     */
    public String getMailForCompletion() {
        String value = settings.getParameters().get(MAIL_FOR_COMPLETION);
        if (value == null || value.trim().length() == 0) {
            value = null;
        }
        return value;
    }

    public String getStackConfigurationAsString() throws HeatException {
        StringBuffer details = new StringBuffer();
        details.append("\t\r\nStackName: ");
        details.append(getStackName());
        details.append("\t\r\nStackId: ");
        details.append(getStackId());
        details.append("\t\r\nAPIUserName: ");
        details.append(getUserName());
        details.append("\t\r\nKeystoneAPIUrl: ");
        details.append(getKeystoneUrl());
        details.append("\t\r\nTenantName: ");
        details.append(getTenantName());
        details.append("\t\r\nTemplateUrl: ");
        details.append(getTemplateUrl());
        details.append("\t\r\nAccessInfoPattern: ");
        details.append(getAccessInfoPattern());
        details.append("\t\r\n");
        return details.toString();
    }

    /**
     * Returns service interfaces for BSS web service calls.
     */
    public <T> T getWebService(Class<T> serviceClass) throws Exception {
        return BSSWebServiceFactory.getBSSWebService(serviceClass,
                settings.getAuthentication());
    }

    /**
     * Returns the instance or controller specific technology manager
     * authentication.
     */
    public PasswordAuthentication getTPAuthentication() {
        return settings.getAuthentication();
    }
    
    /**
     * Returns the locale set as default for the customer organization.
     * 
     * @return the customer locale
     */
    public String getCustomerLocale() {
        String locale = settings.getLocale();
        if (locale == null || locale.trim().length() == 0) {
            locale = "en";
        }
        return locale;
    }
}
