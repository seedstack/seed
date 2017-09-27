/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * The data manager provides data import and export facilities.
 */
public interface DataManager {
    /**
     * Export all data of the application as a stream.
     *
     * @param outputStream the stream to output data to.
     */
    void exportData(OutputStream outputStream);

    /**
     * Export data from one specified group as a stream.
     *
     * @param outputStream the stream to output data to.
     * @param group        the data group.
     */
    void exportData(OutputStream outputStream, String group);

    /**
     * Export a specified data set as a stream.
     *
     * @param outputStream the stream to output data to.
     * @param group        the data group.
     * @param name         the data name.
     */
    void exportData(OutputStream outputStream, String group, String name);

    /**
     * Import data in the application from a stream.
     *
     * @param inputStream the stream to read data from.
     * @param group       the data group, can be null if all groups are accepted.
     * @param name        the name group, can be null if all data sets are accepted.
     * @param clear       clear the existing data upon successful import.
     */
    void importData(InputStream inputStream, String group, String name, boolean clear);

    /**
     * Returns if a particular data set is already initialized in the application. This
     * method is used by SEED to know if it must automatically load initialization data.
     *
     * @param group the data group.
     * @param name  the data name.
     * @return true if already initialized, false otherwise.
     */
    boolean isInitialized(String group, String name);
}
