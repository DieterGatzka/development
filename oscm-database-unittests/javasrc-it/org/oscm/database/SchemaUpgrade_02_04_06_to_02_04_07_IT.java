/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_04_06_to_02_04_07_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_04_06_to_02_04_07_IT() {
        super(new DatabaseVersionInfo(2, 4, 6),
                new DatabaseVersionInfo(2, 4, 7));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_04_06_to_02_04_07.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_04_06_to_02_04_07.xml");
    }
}
