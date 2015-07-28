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

import com.zaxxer.hikari.HikariDataSource;
import org.seedstack.seed.persistence.jdbc.spi.DataSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Data source provider for Hikari.
 *
 * @author yves.dautremay@mpsa.com
 */
public class HikariDataSourceProvider implements DataSourceProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(HikariDataSourceProvider.class);

    @Override
    public DataSource provide(String driverClass, String url, String user, String password, Properties dataSourceProperties) {
        HikariDataSource ds = new HikariDataSource();

        ds.setDriverClassName(driverClass);
        ds.setJdbcUrl(url);
        ds.setUsername(user);
        ds.setPassword(password);
        ds.setDataSourceProperties(dataSourceProperties);

        return ds;
    }

    @Override
    public void close(DataSource dataSource) {
        try {
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to close datasource", e);
        }
    }
}
