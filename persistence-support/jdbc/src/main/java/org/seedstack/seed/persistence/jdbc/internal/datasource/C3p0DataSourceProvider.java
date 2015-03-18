/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 18 févr. 2015
 */
package org.seedstack.seed.persistence.jdbc.internal.datasource;

import java.beans.PropertyVetoException;
import java.util.Properties;

import javax.sql.DataSource;

import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.persistence.jdbc.internal.JdbcErrorCode;
import org.seedstack.seed.persistence.jdbc.spi.DataSourceProvider;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Datasource provider for C3p0
 */
public class C3p0DataSourceProvider implements DataSourceProvider {

    @Override
    public DataSource provideDataSource(String driverClass, String url, String user, String password, Properties jdbcProperties) {
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        try {
            cpds.setDriverClass(driverClass);
            cpds.setJdbcUrl(url);
            cpds.setUser(url);
            cpds.setPassword(user);
            cpds.setProperties(jdbcProperties);
            return cpds;
        } catch (PropertyVetoException e) {
            throw SeedException.wrap(e, JdbcErrorCode.WRONG_JDBC_DRIVER);
        }
    }

}
