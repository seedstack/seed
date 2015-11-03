/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.data;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import org.seedstack.seed.DataManager;

import java.util.Map;

/**
 * Guice module for configuring SEED core data infrastructure.
 *
 * @author pierre.thirouin@ext.mpsa.com
 */
class DataModule extends PrivateModule {

    private final Map<String, Map<String, DataExporterDefinition<Object>>> allDataExporters;

    private final Map<String, Map<String, DataImporterDefinition<Object>>> allDataImporters;


    DataModule(Map<String, Map<String, DataExporterDefinition<Object>>> allDataExporters, Map<String, Map<String, DataImporterDefinition<Object>>> allDataImporters) {
        this.allDataExporters = allDataExporters;
        this.allDataImporters = allDataImporters;
    }

    @Override
    protected void configure() {
        bind(new TypeLiteral<Map<String, Map<String, DataExporterDefinition<Object>>>>() {}).toInstance(allDataExporters);
        bind(new TypeLiteral<Map<String, Map<String, DataImporterDefinition<Object>>>>() {}).toInstance(allDataImporters);

        bind(DataManager.class).to(DataManagerImpl.class);

        // Bind importers
        for (Map<String, DataImporterDefinition<Object>> dataImporterDefinitionMap : allDataImporters.values()) {
            for (DataImporterDefinition<Object> dataImporterDefinition : dataImporterDefinitionMap.values()) {
                bind(dataImporterDefinition.getDataImporterClass());
            }
        }

        // Bind exporters
        for (Map<String, DataExporterDefinition<Object>> dataExporterDefinitionMap : allDataExporters.values()) {
            for (DataExporterDefinition<Object> dataExporterDefinition : dataExporterDefinitionMap.values()) {
                bind(dataExporterDefinition.getDataExporterClass());
            }
        }

        expose(DataManager.class);
    }
}
