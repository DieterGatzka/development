/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                        
 *                                                                              
 *  Creation Date: 2012-08-06                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v1_0.data;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Wrapper object for service parameters and configuration settings. Service
 * parameters are defined at the technical service for an application; values
 * for the parameters may be specified at subscriptions and evaluated by the
 * application. Configuration settings are the settings and values defined for
 * an application-specific service controller; they can also be evaluated by the
 * application.
 */
public class ProvisioningSettings extends ControllerSettings implements
        Serializable {

    private static final long serialVersionUID = 9161029657174458354L;

    private String locale;
    private HashMap<String, String> parameters;
    private String organizationId;
    private String organizationName;
    private String subscriptionId;
    private String besLoginURL;
    private ServiceUser requestingUser;

    /**
     * Constructs a new provisioning settings instance with the given service
     * parameters and controller configuration settings. The specified locale is
     * used for language-dependent strings.
     * 
     * @param parameters
     *            the service parameters, consisting of a key and a value each
     * @param configSettings
     *            the configuration settings, consisting of a key and a value
     *            each
     * @param locale
     *            the language. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     */
    public ProvisioningSettings(HashMap<String, String> parameters,
            HashMap<String, String> configSettings, String locale) {
        super(configSettings);
        this.parameters = parameters;
        this.locale = locale;
    }

    /**
     * Returns a list of the service parameters.
     * 
     * @return the service parameters, consisting of a key and a value each
     */
    public HashMap<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets the service parameters.
     * 
     * @param parameters
     *            the service parameters, consisting of a key and a value each
     */
    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns the locale used for language-dependent strings.
     * 
     * @return the language code
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Returns the identifier of the customer organization which created the
     * subscription.
     * 
     * @return the organization ID
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the identifier of the customer organization which created the
     * subscription.
     * 
     * @param organizationId
     *            the organization ID
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Returns the name of the customer organization which created the
     * subscription.
     * 
     * @return the organization name
     */
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * Sets the name of the customer organization which created the
     * subscription.
     * 
     * @param organizationName
     *            the organization name
     */
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    /**
     * Returns the name specified by the customer to identify the subscription.
     * 
     * @return the subscription ID
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Returns the name specified by the customer to identify the subscription.
     * 
     * @return the subscription ID
     */
    public String getOriginalSubscriptionId() {
        int index = subscriptionId.indexOf("#");
        if (index >= 0) {
            return subscriptionId.substring(0, index);
        } else {
            return subscriptionId;
        }
    }

    /**
     * Sets the name specified by the customer to identify the subscription.
     * 
     * @param subscriptionId
     *            the subscription ID
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * Returns the platform's login page to which the application can redirect
     * users who need to log in. This value is set for the <code>LOGIN</code>
     * access types.
     * <p>
     * Typically, the login page is displayed when a user tries to directly
     * access the application before logging in to the platform first, or when a
     * user needs to log in again because his previous session has timed out.
     * 
     * @return the login URL
     */
    public String getBesLoginURL() {
        return besLoginURL;
    }

    /**
     * Sets the platform's login page to which the application can redirect
     * users who need to log in. This value is set for the <code>LOGIN</code>
     * access type.
     * <p>
     * Typically, the login page is displayed when a user tries to directly
     * access the application before logging in to the platform first, or when a
     * user needs to log in again because his previous session has timed out.
     * 
     * @param loginUrl
     *            the URL of the platform's login page
     */
    public void setBesLoginUrl(String loginUrl) {
        this.besLoginURL = loginUrl;
    }

    /**
     * <p>If APP calls createInstance() of the controller, returns the user
     * who requested the current provisioning operation.</p>
     * 
     * <p>If a timer calls the controller to switch from one status to the
     * other, returns 'null'.</p>
     * 
     * @return the user or 'null'
     */
    public ServiceUser getRequestingUser() {
        return requestingUser;
    }

    /**
     * Set the user who requested a the current provisioning operation.
     * 
     * @param user
     *            the user
     */
    public void setRequestingUser(ServiceUser user) {
        this.requestingUser = user;
    }

}
