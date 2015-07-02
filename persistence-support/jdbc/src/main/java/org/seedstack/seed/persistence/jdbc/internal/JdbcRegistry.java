/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.jdbc.internal;

import javax.sql.DataSource;

/**
 * The JDBC registry is an internal API for accessing configured JDBC data sources.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public interface JdbcRegistry {

    /**
     * This method allows to automatically use a data source for the given class when it asks for the injection of a connection.
     *
     * @param dataSourceName the dataSource to use
     * @param clazz          the class requiring a connection
     */
    void registerDataSourceForClass(Class<?> clazz, String dataSourceName);

    /**
     * Provides a configured data source by its name.
     *
     * @param dataSource the data source name
     * @return the dataSource
     */
    DataSource getDataSource(String dataSource);
}
