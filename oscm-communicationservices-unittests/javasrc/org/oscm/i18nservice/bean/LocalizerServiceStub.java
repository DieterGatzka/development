/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                              
 *  Creation Date: 13.05.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.i18nservice.bean;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizedDomainObject;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.vo.VOLocalizedText;

/**
 * Stub for communication service unit tests.
 * 
 */
@Stateless
@Local(LocalizerServiceLocal.class)
public class LocalizerServiceStub implements LocalizerServiceLocal {

    public Locale getDefaultLocale() {
        return Locale.ENGLISH;
    }

    public void checkExistenceOfBundleFiles() {

    }

    public String getLocalizedTextFromBundle(LocalizedObjectTypes objectType,
            Marketplace shop, String localeString, String key) {
        return "pseudo-stub-return-value";
    }

    public String getLocalizedTextFromDatabase(String localeString,
            long objectKey, LocalizedObjectTypes objectType) {

        return null;
    }

    public boolean storeLocalizedResource(String localeString, long objectKey,
            LocalizedObjectTypes objectType, String value) {

        return false;
    }

    public List<VOLocalizedText> getLocalizedValues(long objectKey,
            LocalizedObjectTypes objectType) {

        return null;
    }

    public void setLocalizedValues(long key,
            LocalizedObjectTypes pricemodel_description,
            List<VOLocalizedText> values) {

    }

    public void removeLocalizedValues(long objectKey, LocalizedObjectTypes type) {

    }

    public Properties loadLocalizedPropertiesFromDatabase(long objectKey,
            LocalizedObjectTypes type, String localeString) {

        return null;
    }

    public Properties loadLocalizedPropertiesFromFile(String baseName,
            String localString) {

        return null;
    }

    public void storeLocalizedResources(long objectKey,
            LocalizedObjectTypes objectType, List<VOLocalizedText> values) {

    }

    public Map<LocalizedObjectTypes, String> getLocalizedTextFromDatabase(
            String localeString, long objectKey,
            List<LocalizedObjectTypes> objectTypes) {

        return null;
    }

    public void removeLocalizedValue(long objectKey,
            LocalizedObjectTypes objectType, String localeString) {

    }

    public List<LocalizedDomainObject> getLocalizedTextFromDatabase(
            String localeString, List<Long> objectKeys,
            List<LocalizedObjectTypes> objectTypes) {
        return Collections.emptyList();
    }

}
