/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.seedstack.seed.core.fixtures.data.TestDTO;
import org.seedstack.seed.core.fixtures.data.TestDTO2;
import org.seedstack.seed.core.fixtures.data.TestDataExporter;
import org.seedstack.seed.core.fixtures.data.TestDataExporter2;
import org.seedstack.seed.core.fixtures.data.TestDataImporter;
import org.seedstack.seed.core.fixtures.data.TestDataImporter2;
import org.skyscreamer.jsonassert.JSONAssert;

public class DataManagerImplTest {
    public static final String ACTUAL_JSON = "[{\"group\":\"group1\",\"name\":\"test1\","
            + "\"items\":[{\"firstName\":\"toto\",\"lastName\":\"titi\"},{\"firstName\":\"machin\","
            + "\"lastName\":\"truc\"}]},{\"group\":\"group1\",\"name\":\"test2\",\"items\":[{\"firstName\":\"toto2\","
            + "\"lastName\":\"titi2\",\"age\":12}]}]";
    private TestDataImporter testDataImporter;
    private TestDataImporter2 testDataImporter2;
    private TestDataExporter testDataExporter;
    private TestDataExporter2 testDataExporter2;

    @Before
    public void setup() {
        testDataExporter = new TestDataExporter();
        testDataExporter2 = new TestDataExporter2();
        testDataImporter = new TestDataImporter();
        testDataImporter2 = new TestDataImporter2();
    }

    @Test
    public void data_are_exported_correctly() throws Exception {
        DataManagerImpl underTest = createDataManager();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        underTest.exportData(byteArrayOutputStream, "group1");

        JSONAssert.assertEquals(new String(byteArrayOutputStream.toByteArray(), "UTF-8"), ACTUAL_JSON, false);
    }

    @Test
    public void full_data_export_is_working_correctly() throws Exception {
        DataManagerImpl underTest = createDataManager();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
        underTest.exportData(byteArrayOutputStream, "group1");
        underTest.exportData(byteArrayOutputStream2);

        assertThat(byteArrayOutputStream.toByteArray()).isEqualTo(byteArrayOutputStream2.toByteArray());
    }

    @Test
    public void data_are_imported_correctly() throws Exception {
        DataManagerImpl underTest = createDataManager();
        underTest.importData(new ByteArrayInputStream(ACTUAL_JSON.getBytes(Charset.forName("UTF-8"))), null, null,
                true);

        assertThat(TestDataImporter.getData().size()).isEqualTo(2);

        assertThat(TestDataImporter.getData().get(0).getFirstName()).isEqualTo("toto");
        assertThat(TestDataImporter.getData().get(0).getLastName()).isEqualTo("titi");

        assertThat(TestDataImporter.getData().get(1).getFirstName()).isEqualTo("machin");
        assertThat(TestDataImporter.getData().get(1).getLastName()).isEqualTo("truc");

        assertThat(TestDataImporter2.getData().size()).isEqualTo(1);

        assertThat(TestDataImporter2.getData().get(0).getFirstName()).isEqualTo("toto2");
        assertThat(TestDataImporter2.getData().get(0).getLastName()).isEqualTo("titi2");
    }

    private Injector mockInjector() {
        Injector injector = mock(Injector.class);
        when(injector.getInstance(TestDataExporter.class)).thenReturn(testDataExporter);
        when(injector.getInstance(TestDataExporter2.class)).thenReturn(testDataExporter2);
        when(injector.getInstance(TestDataImporter2.class)).thenReturn(testDataImporter2);
        when(injector.getInstance(TestDataImporter.class)).thenReturn(testDataImporter);
        return injector;
    }

    private DataManagerImpl createDataManager() {
        DataManagerImpl yamlDataService = new DataManagerImpl();
        Whitebox.setInternalState(yamlDataService, "injector", mockInjector());
        Whitebox.setInternalState(yamlDataService, "allDataExporters", ImmutableMap.of("group1",
                ImmutableMap.of("test1",
                        new DataExporterDefinition<>("test1", "group1", TestDTO.class, TestDataExporter.class), "test2",
                        new DataExporterDefinition<>("test2", "group1", TestDTO2.class, TestDataExporter2.class))));
        Whitebox.setInternalState(yamlDataService, "allDataImporters", ImmutableMap.of("group1",
                ImmutableMap.of("test1",
                        new DataImporterDefinition<>("test1", "group1", TestDTO.class, TestDataImporter.class), "test2",
                        new DataImporterDefinition<>("test2", "group1", TestDTO2.class, TestDataImporter2.class))));
        return yamlDataService;
    }

}
