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

import org.seedstack.seed.persistence.jdbc.api.JdbcExceptionHandler;
import org.seedstack.seed.persistence.jdbc.spi.DataSourceProvider;

import javax.sql.DataSource;

/**
 * Holds all the objects associated to a JDBC data source.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
class DataSourceDefinition {

    private final String name;

    private Class<? extends JdbcExceptionHandler> jdbcExceptionHandler;

    private DataSourceProvider dataSourceProvider;

    private DataSource dataSource;

    /**
     * Constructor.
     *
     * @param name the data source name
     */
    DataSourceDefinition(String name) {
        this.name = name;
    }

    /**
     * Constructor.
     *
     * @param name       the data source name
     * @param dataSource the dataSource
     */
    DataSourceDefinition(String name, DataSource dataSource) {
        this.name = name;
        this.dataSource = dataSource;
    }

    /**
     * @return the data source name
     */
    String getName() {
        return name;
    }

    /**
     * @return the JDBC exception handler
     */
    Class<? extends JdbcExceptionHandler> getJdbcExceptionHandler() {
        return jdbcExceptionHandler;
    }

    void setJdbcExceptionHandler(Class<? extends JdbcExceptionHandler> jdbcExceptionHandler) {
        this.jdbcExceptionHandler = jdbcExceptionHandler;
    }

    DataSourceProvider getDataSourceProvider() {
        return dataSourceProvider;
    }

    void setDataSourceProvider(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    DataSource getDataSource() {
        return dataSource;
    }

    void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
