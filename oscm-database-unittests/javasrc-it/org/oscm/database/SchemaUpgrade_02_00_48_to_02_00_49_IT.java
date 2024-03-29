/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_00_48_to_02_00_49_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_48_to_02_00_49_IT() {
        super(new DatabaseVersionInfo(2, 0, 48), new DatabaseVersionInfo(2, 0,
                49));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_48_to_02_00_49.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_48_to_02_00_49.xml");
    }
}
