/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.nuun.kernel.api.Kernel;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seedstack.seed.core.api.DataManager;
import org.seedstack.seed.core.fixtures.TestDataImporter;
import org.seedstack.seed.core.fixtures.TestDataImporter2;
import org.seedstack.seed.core.fixtures.TestDataImporter3;
import org.seedstack.seed.core.utils.SeedReflectionUtils;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;

import static io.nuun.kernel.core.NuunCore.createKernel;
import static io.nuun.kernel.core.NuunCore.newKernelConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 12/03/14
 */
public class DataManagerIT {

    static Kernel underTest;
    static Holder holder;

    static class Holder {
        @Inject
        DataManager dataManager;
    }

    @Test
    public void data_manager_is_injected() {
        Assertions.assertThat(holder).isNotNull();
        Assertions.assertThat(holder.dataManager).isNotNull();
    }

    @Test
    public void data_are_exported_correctly() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
        holder.dataManager.exportData(byteArrayOutputStream, "group1");
        holder.dataManager.exportData(byteArrayOutputStream2);

        assertThat(byteArrayOutputStream.toByteArray()).isEqualTo(byteArrayOutputStream2.toByteArray());
    }

    @Test
    public void data_are_imported_correctly() throws Exception {
        holder.dataManager.importData(SeedReflectionUtils.findMostCompleteClassLoader(DataManagerIT.class).getResourceAsStream("test_data.json"), null, null, true);

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

    @BeforeClass
    public static void setup() {
        underTest = createKernel(newKernelConfiguration());
        underTest.init();
        underTest.start();

        Module aggregationModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Holder.class);
            }
        };

        holder = underTest.objectGraph().as(Injector.class).createChildInjector(aggregationModule).getInstance(Holder.class);
    }

    @AfterClass
    public static void teardown() {
        underTest.stop();
        underTest = null;
        holder = null;
    }
}
