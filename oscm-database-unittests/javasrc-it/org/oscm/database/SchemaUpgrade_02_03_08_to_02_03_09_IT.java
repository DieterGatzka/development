/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_03_08_to_02_03_09_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_03_08_to_02_03_09_IT() {
        super(new DatabaseVersionInfo(2, 3, 8),
                new DatabaseVersionInfo(2, 3, 9));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_03_08_to_02_03_09.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_03_08_to_02_03_09.xml");
    }

}
