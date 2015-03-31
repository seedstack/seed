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
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

class JdbcModule extends PrivateModule {
    private static final String JDBC_REGISTERED_CLASSES = "jdbc-registered-classes";

    private final Map<String, DataSource> dataSources;
    private final Map<String, Class<? extends JdbcExceptionHandler>> jdbcExceptionHandlerClasses;
    private final Map<Class<?>, String> registeredClasses;

    JdbcModule(Map<String, DataSource> dataSources, Map<String, Class<? extends JdbcExceptionHandler>> jdbcExceptionHandlerClasses, Map<Class<?>, String> registeredClasses) {
        this.dataSources = dataSources;
        this.jdbcExceptionHandlerClasses = jdbcExceptionHandlerClasses;
        this.registeredClasses = registeredClasses;
    }

    @Override
    protected void configure() {
        // Connection
        JdbcConnectionLink jdbcLink = new JdbcConnectionLink();
        bind(Connection.class).toInstance(TransactionalProxy.create(Connection.class, jdbcLink));

        // Datasources
        for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            bindDataSource(entry.getKey(), entry.getValue(), jdbcLink);
        }

        // Classes registered to use a specific datasource
        bind(new TypeLiteral<Map<Class<?>, String>>() {}).annotatedWith(Names.named(JDBC_REGISTERED_CLASSES)).toInstance(registeredClasses);

        expose(Connection.class);
        expose(new TypeLiteral<Map<Class<?>, String>>() {}).annotatedWith(Names.named(JDBC_REGISTERED_CLASSES));
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
