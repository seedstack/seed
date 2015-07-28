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

import org.seedstack.seed.persistence.jdbc.spi.DataSourceProvider;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Data source provider for the {@link PlainDataSource}.
 *
 * @author yves.dautremay@mpsa.com
 */
public class PlainDataSourceProvider implements DataSourceProvider {

    @Override
    public DataSource provide(String driverClass, String url, String user, String password, Properties dataSourceProperties) {
        return new PlainDataSource(driverClass, url, user, password, dataSourceProperties);
    }

    @Override
    public void close(DataSource dataSource) {
        // not supported
    }
}
