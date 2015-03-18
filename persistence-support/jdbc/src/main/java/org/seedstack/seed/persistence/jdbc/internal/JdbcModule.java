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
 * Creation : 17 févr. 2015
 */
package org.seedstack.seed.persistence.jdbc.internal;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import org.seedstack.seed.persistence.jdbc.api.JdbcExceptionHandler;
import org.seedstack.seed.transaction.utils.TransactionalProxy;

import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

/**
 * JDBC support module
 */
public class JdbcModule extends PrivateModule {

    private Map<String, DataSource> dataSources;

    private Map<String, Class<? extends JdbcExceptionHandler>> jdbcExceptionHandlerClasses;

    JdbcModule(Map<String, DataSource> dataSources, Map<String, Class<? extends JdbcExceptionHandler>> jdbcExceptionHandlerClasses) {
        this.dataSources = dataSources;
        this.jdbcExceptionHandlerClasses = jdbcExceptionHandlerClasses;
    }

    @Override
    protected void configure() {
        JdbcConnectionLink jdbcLink = new JdbcConnectionLink();
        bind(Connection.class).toInstance(TransactionalProxy.create(Connection.class, jdbcLink));

        for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            bindDataSource(entry.getKey(), entry.getValue(), jdbcLink);
        }

        expose(Connection.class);
    }

    private void bindDataSource(String name, DataSource dataSource, JdbcConnectionLink jdbcLink) {
        Class<? extends JdbcExceptionHandler> jdbcExceptionHandlerClass = jdbcExceptionHandlerClasses.get(name);

        if (jdbcExceptionHandlerClass != null) {
            bind(JdbcExceptionHandler.class).annotatedWith(Names.named(name)).to(jdbcExceptionHandlerClass);
        } else {
            bind(JdbcExceptionHandler.class).annotatedWith(Names.named(name)).toProvider(Providers.<JdbcExceptionHandler> of(null));
        }

        JdbcTransactionHandler transactionHandler = new JdbcTransactionHandler(jdbcLink, dataSource);
        bind(JdbcTransactionHandler.class).annotatedWith(Names.named(name)).toInstance(transactionHandler);

        expose(JdbcExceptionHandler.class).annotatedWith(Names.named(name));
        expose(JdbcTransactionHandler.class).annotatedWith(Names.named(name));
    }

}
