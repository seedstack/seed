/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.spi.data;

/**
 * Implement this interface to create a data set importer that will handle objects of a specific type. A data importer
 * must be marked with a {@link DataSet} annotation to be recognized.
 *
 * @author adrien.lauer@mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 * @param <T> the type this data importer handles.
 */
public interface DataImporter<T> {
    /**
     * This method is used by SEED to determine if a data importer should be automatically initialized with data.
     *
     * @return true if already initialized (and as such won't be automatically initialized), false otherwise.
     */
    boolean isInitialized();

    /**
     * This method is called by SEED to import an object handled by this importer.
     *
     * @param data the object to import.
     */
    void importData(T data);

    /**
     * This method is called when the import operation is successful and imported data should be committed.
     *
     * @param clear true if existing data must be cleared before commit.
     */
    void commit(boolean clear);

    /**
     * This method is called when the import operation suffered an error and imported data should be discarded or
     * deleted.
     */
    void rollback();
}
