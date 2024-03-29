/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 10.10.2011                                                      
 *                                                                              
 *  Completion Time: 10.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.adapter;

import java.io.IOException;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.triggerservice.data.SupportedVersions;
import org.oscm.ws.WSPortConnector;
import org.oscm.ws.WSPortDescription;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Retrieves the notification service adapter according to the concrete WSDL.
 * 
 * @author weiser
 * 
 */
public class NotificationServiceAdapterFactory {

    public static INotificationServiceAdapter getNotificationServiceAdapter(
            TriggerDefinition td, Integer wsTimeout,
            ConfigurationServiceLocal cs, DataService ds) throws IOException,
            WSDLException, ParserConfigurationException {

        String target = td.getTarget();
        if (target == null) {
            throw new SaaSSystemException(
                    String.format(
                            "Failed to retrieve service endpoint for trigger definition '%s', as the target is not defined.",
                            Long.valueOf(td.getKey())));
        }
        WSPortConnector portConnector = new WSPortConnector(target, null, null);

        SupportedVersions supportedVersion = getSupportedVersion(portConnector);
        INotificationServiceAdapter adapter = getAdapterForVersion(
                supportedVersion, ds);

        final Object port = portConnector.getPort(
                supportedVersion.getLocalWSDL(),
                supportedVersion.getServiceClass(), wsTimeout);
        initAdapter(cs, ds, adapter, port);

        return adapter;
    }

    static void initAdapter(ConfigurationServiceLocal cs, DataService ds,
            INotificationServiceAdapter adapter, final Object port) {
        adapter.setNotificationService(port);
        adapter.setConfigurationService(cs);
        adapter.setDataService(ds);
    }

    static SupportedVersions getSupportedVersion(WSPortConnector portConnector) {
        WSPortDescription portDescription = portConnector.getPortDescription();
        String targetVersionFromWsdl = portDescription.getVersion();
        SupportedVersions supportedVersion = SupportedVersions
                .getForVersionString(targetVersionFromWsdl);
        if (supportedVersion == null) {
            String targetNamespaceFromWsdl = portDescription
                    .getTargetNamespace();
            supportedVersion = SupportedVersions
                    .getForNamespaceString(targetNamespaceFromWsdl);
            if (supportedVersion == null) {
                String message = "Unsupported namespace for NotifcationService: %s";
                throw new SaaSSystemException(String.format(message,
                        targetNamespaceFromWsdl));
            }
        }
        return supportedVersion;
    }

    /**
     * Initialized a notification service adapter for the version matching the
     * the version retrieved from wsdl.
     * 
     * @param supportedVersion
     *            the version retrieved from wsdl
     * @param ds
     *            the data source
     * @return the {@link INotificationServiceAdapter}
     */
    static INotificationServiceAdapter getAdapterForVersion(
            SupportedVersions supportedVersion, DataService ds) {
        switch (supportedVersion) {
        case CURRENT_VERSION:
            return new NotificationServiceAdapter();
        default:
            NotificationServiceAdapter adapter = new NotificationServiceAdapter();
            return adapter;
        }
    }
}
