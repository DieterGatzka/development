/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                                                                                 
 *  Creation Date: 2012-6-11                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageudas;

/**
 * @author yuyin
 * 
 */
public class UdaDefinitionDetails {
    /**
     * The identifier of the custom attribute; must be unique for the target
     * type.
     */
    private String udaId;

    /**
     * The default value for the custom attribute
     */
    private String defaultValue;

    /**
     * Determine the UDA visible or not.
     */
    private boolean userOption;

    /**
     * Determine the UDA value is mandatory or not.
     */
    private boolean mandatory;

    /**
     * the numeric key for the UDA
     */
    private long key;

    /**
     * The version of the UDA
     */
    private int version;

    /**
     * @return the udaId
     */
    public String getUdaId() {
        return udaId;
    }

    /**
     * @param udaId
     *            the udaId to set
     */
    public void setUdaId(String udaId) {
        this.udaId = udaId;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue
     *            the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the userOption
     */
    public boolean isUserOption() {
        return userOption;
    }

    /**
     * @param userOption
     *            the userOption to set
     */
    public void setUserOption(boolean userOption) {
        this.userOption = userOption;
        this.mandatory = mandatory && userOption;
    }

    /**
     * @return the mandatory
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * @param mandatory
     *            the mandatory to set
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * @return the key
     */
    public long getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(long key) {
        this.key = key;
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }
}
