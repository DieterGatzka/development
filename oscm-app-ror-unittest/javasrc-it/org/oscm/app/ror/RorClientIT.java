/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                                                                                 
 *  Creation Date: 22.01.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.ror;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.oscm.app.iaas.PropertyHandler;
import org.oscm.app.ror.client.LPlatformClient;
import org.oscm.app.ror.client.RORClient;
import org.oscm.app.ror.data.LPlatformConfiguration;
import org.oscm.app.ror.data.LPlatformDescriptor;
import org.oscm.app.ror.data.LPlatformDescriptorConfiguration;
import org.oscm.app.ror.data.LServerConfiguration;
import org.oscm.app.v1_0.data.ProvisioningSettings;

/**
 * Intention: Use this class manually to retrieve information from a running ROR
 * hardware simulator. This class should not be used for any automated tests.<br />
 * <br />
 * Note: In order to access the ROR API we need the ROR domain certificate, it
 * can be found in the javares folder, see ror.jks. If the ROR installation
 * changes it might be necessary to update the local ROR keystore.
 * 
 * @author kulle
 * 
 */
@Ignore
public class RorClientIT {

    protected String IAAS_API_URI = "https://10.140.18.34:8014/cfmgapi/endpoint";
    protected RORClient vdcClient;
    protected LPlatformClient platformClient;
    protected ProvisioningSettings settings;

    @BeforeClass
    public static void setupOnce() {
        URL keystorePath = RorClientIT.class.getResource("/ror.jks");
        System.setProperty("javax.net.ssl.trustStore", keystorePath.getPath());
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
    }

    @Before
    public void setup() throws Exception {
        settings = newProvisioningSettings();
        vdcClient = new RORVServerCommunication()
                .getVdcClient(new PropertyHandler(settings));
    }

    private ProvisioningSettings newProvisioningSettings() {
        ProvisioningSettings settings = new ProvisioningSettings(
                new HashMap<String, String>(), new HashMap<String, String>(),
                "en");

        settings.getParameters().put(PropertyHandler.VSYS_ID,
                "SampleTe-IZ3MDGZSB");
        settings.getParameters().put(PropertyHandler.VSERVER_ID,
                "SampleTe-IZ3MDGZSB-S-0001");

        settings.getConfigSettings().put(PropertyHandler.IAAS_API_LOCALE, "en");
        settings.getConfigSettings().put(PropertyHandler.IAAS_API_URI,
                IAAS_API_URI);
        settings.getConfigSettings().put(PropertyHandler.IAAS_API_TENANT,
                "SampleTenant");
        settings.getConfigSettings().put(PropertyHandler.IAAS_API_USER,
                "tenant_admin");
        settings.getConfigSettings().put(PropertyHandler.IAAS_API_PWD,
                "tenantadmin");

        return settings;
    }

    @Test
    public void retrieveTemplateInformation() throws Exception {
        List<LPlatformDescriptor> descriptors = vdcClient
                .listLPlatformDescriptors();
        for (LPlatformDescriptor lpd : descriptors) {
            System.out.println("PLATFORM DESCRIPTOR:");
            System.out.println("ID:\t" + lpd.getVSystemTemplateId());
            System.out.println("Name:\t" + lpd.getVSystemTemplateName());
            LPlatformDescriptorConfiguration configuration = vdcClient
                    .getLPlatformDescriptorConfiguration(lpd
                            .getVSystemTemplateId());
            for (LServerConfiguration lsc : configuration.getVServers()) {
                System.out.println(lsc.toString());
            }
            System.out.println("");
        }
    }

    @Test
    public void retrievePlatformInformation() throws Exception {
        List<LPlatformConfiguration> platforms = vdcClient.listLPlatforms(true);
        for (LPlatformConfiguration platformConfiguration : platforms) {
            System.out.println("LPLATFORM CONFIGURATION:");
            System.out.println("ID:\t" + platformConfiguration.getVSystemId());
            System.out.println("Name:\t"
                    + platformConfiguration.getVSystemName());
            System.out.println("Stati:\t"
                    + platformConfiguration.getServerStatus());
            System.out.println("networkIDs: "
                    + platformConfiguration.getNetworks());
            System.out.println("");

            settings.getParameters().put(PropertyHandler.VSYS_ID,
                    platformConfiguration.getVSystemId());
            platformClient = new RORVServerCommunication()
                    .getLPlatformClient(new PropertyHandler(settings));
            for (LServerConfiguration serverConfiguration : platformClient
                    .getConfiguration().getVServers()) {
                System.out.println(serverConfiguration.toString());
            }
            System.out.println("\n");
        }
    }
}
