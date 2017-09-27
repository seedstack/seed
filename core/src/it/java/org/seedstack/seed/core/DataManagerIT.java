/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.seedstack.seed.DataManager;
import org.seedstack.seed.core.fixtures.TestDataImporter3;
import org.seedstack.seed.core.fixtures.data.TestDataImporter;
import org.seedstack.seed.core.fixtures.data.TestDataImporter2;
import org.seedstack.seed.core.rules.SeedITRule;
import org.seedstack.shed.ClassLoaders;

public class DataManagerIT {
    @Rule
    public SeedITRule rule = new SeedITRule(this);

    @Inject
    DataManager dataManager;

    @Test
    public void data_manager_is_injected() {
        Assertions.assertThat(dataManager).isNotNull();
    }

    @Test
    public void data_are_exported_correctly() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
        dataManager.exportData(byteArrayOutputStream, "group1");
        dataManager.exportData(byteArrayOutputStream2);

        assertThat(byteArrayOutputStream.toByteArray()).isEqualTo(byteArrayOutputStream2.toByteArray());
    }

    @Test
    public void data_are_imported_correctly() throws Exception {
        dataManager.importData(
                ClassLoaders.findMostCompleteClassLoader(DataManagerIT.class).getResourceAsStream("test_data.json"),
                null, null, true);

        assertThat(TestDataImporter.getData().size()).isEqualTo(2);

        assertThat(TestDataImporter.getData().get(0).getFirstName()).isEqualTo("toto");
        assertThat(TestDataImporter.getData().get(0).getLastName()).isEqualTo("titi");

        assertThat(TestDataImporter.getData().get(1).getFirstName()).isEqualTo("machin");
        assertThat(TestDataImporter.getData().get(1).getLastName()).isEqualTo("truc");

        assertThat(TestDataImporter2.getData().size()).isEqualTo(0);
        //
        //        assertThat(TestDataImporter2.getData().get(0).getFirstName()).isEqualTo("toto2");
        //        assertThat(TestDataImporter2.getData().get(0).getLastName()).isEqualTo("titi2");
    }

    @Test
    public void initialization_data_is_imported_upon_startup() throws Exception {
        assertThat(TestDataImporter3.getData().size()).isEqualTo(1);

        assertThat(TestDataImporter3.getData().get(0).getFirstName()).isEqualTo("toto2");
        assertThat(TestDataImporter3.getData().get(0).getLastName()).isEqualTo("titi2");
    }
}
