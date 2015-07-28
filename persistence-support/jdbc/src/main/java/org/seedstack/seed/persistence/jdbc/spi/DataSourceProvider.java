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
package org.seedstack.seed.persistence.jdbc.spi;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Interface for data source providers. The role of a datasource provider is to create a datasource the jdbc support
 * will be able to use.
 *
 * @author yves.dautremay@mpsa.com
 */
public interface DataSourceProvider {

    /**
     * Provides a datasource
     *
     * @param driverClass    configured driver
     * @param url            configured url
     * @param user           configured user
     * @param password       configured password
     * @param jdbcProperties Additional configured properties
     * @return the datasource
     */
    DataSource provide(String driverClass, String url, String user, String password, Properties jdbcProperties);

    /**
     * This method is called upon shutdown to allow the provider to close a datasource in an implementation-specific
     * way.
     *
     * @param dataSource the datasource that may be closed.
     */
    void close(DataSource dataSource);
}
