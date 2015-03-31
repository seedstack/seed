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

import java.util.Properties;

import javax.sql.DataSource;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.apache.commons.dbcp.BasicDataSource;
import org.seedstack.seed.persistence.jdbc.spi.DataSourceProvider;

/**
 * Data source provider for commons DBCP.
 *
 * @author yves.dautremay@mpsa.com
 */
public class DbcpDataSourceProvider implements DataSourceProvider {

    @Override
    public DataSource provideDataSource(String driverClass, String url, String user, String password, Properties dataSourceProperties) {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(driverClass);
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(user);
        basicDataSource.setPassword(password);
        for (Object key : dataSourceProperties.keySet()) {
            basicDataSource.addConnectionProperty((String) key, dataSourceProperties.getProperty((String) key));
        }
        return basicDataSource;
    }

    @Override
    public void setHealthCheckRegistry(HealthCheckRegistry healthCheckRegistry) {
        // not supported
    }

    @Override
    public void setMetricRegistry(MetricRegistry metricRegistry) {
        // not supported
    }
}
