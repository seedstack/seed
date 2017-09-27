/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed;

import java.util.Iterator;

/**
 * Implement this interface to create a data set exporter that will handle objects of a specific type. A data exporter
 * must be marked with a {@link DataSet} annotation to be recognized.
 *
 * @param <T> the type this data exporter handles.
 */
public interface DataExporter<T> {
    /**
     * This method is called by SEED to export data handled by this exporter. A lazy iterator of all data to export
     * should be returned.
     *
     * @return the lazy iterator of all data to export.
     */
    Iterator<T> exportData();
}
