/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.data;

import org.seedstack.seed.DataImporter;

/**
 * Holds the definition of a {@link DataImporter}.
 *
 * @param <T> the imported data type.
 */
class DataImporterDefinition<T> {
    private final String name;
    private final String group;
    private final Class<T> importedClass;
    private final Class<? extends DataImporter<T>> dataImporterClass;

    DataImporterDefinition(String name, String group, Class<T> importedClass,
            Class<? extends DataImporter<T>> dataImporterClass) {
        this.name = name;
        this.group = group;
        this.importedClass = importedClass;
        this.dataImporterClass = dataImporterClass;
    }

    Class<T> getImportedClass() {
        return importedClass;
    }

    Class<? extends DataImporter<T>> getDataImporterClass() {
        return dataImporterClass;
    }

    String getGroup() {
        return group;
    }

    String getName() {
        return name;
    }

}
