/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_02_19_to_02_02_20_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_02_19_to_02_02_20_IT() {
        super(new DatabaseVersionInfo(2, 2, 19), new DatabaseVersionInfo(2, 2,
                20));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_02_19_to_02_02_20.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_02_19_to_02_02_20.xml");
    }

}
