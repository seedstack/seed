/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.jpa.internal;

import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import org.seedstack.seed.persistence.jpa.api.JpaExceptionHandler;
import org.seedstack.seed.transaction.spi.TransactionConcern;
import org.seedstack.seed.transaction.utils.TransactionalProxy;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Map;

@TransactionConcern
class JpaModule extends PrivateModule {
    private final Map<String, EntityManagerFactory> entityManagerFactories;
    private final Map<String, Class<? extends JpaExceptionHandler>> jpaExceptionHandlerClasses;

    JpaModule(Map<String, EntityManagerFactory> entityManagerFactories, Map<String, Class<? extends JpaExceptionHandler>> jpaExceptionHandlerClasses) {
        this.entityManagerFactories = entityManagerFactories;
        this.jpaExceptionHandlerClasses = jpaExceptionHandlerClasses;
    }

    @Override
    protected void configure() {
        EntityManagerLink entityManagerLink = new EntityManagerLink();
        bind(EntityManager.class).toInstance(TransactionalProxy.create(EntityManager.class, entityManagerLink));

        for (Map.Entry<String, EntityManagerFactory> entry : entityManagerFactories.entrySet()) {
            bindUnit(entry.getKey(), entry.getValue(), entityManagerLink);
        }

        expose(EntityManager.class);
    }

    private void bindUnit(String name, EntityManagerFactory entityManagerFactory, EntityManagerLink entityManagerLink) {
        Class<? extends JpaExceptionHandler> unitExceptionHandlerClass = jpaExceptionHandlerClasses.get(name);

        if (unitExceptionHandlerClass != null) {
            bind(JpaExceptionHandler.class).annotatedWith(Names.named(name)).to(unitExceptionHandlerClass);
        } else {
            bind(JpaExceptionHandler.class).annotatedWith(Names.named(name)).toProvider(Providers.<JpaExceptionHandler>of(null));
        }

        JpaTransactionHandler transactionHandler = new JpaTransactionHandler(entityManagerLink, entityManagerFactory);
        bind(JpaTransactionHandler.class).annotatedWith(Names.named(name)).toInstance(transactionHandler);

        expose(JpaExceptionHandler.class).annotatedWith(Names.named(name));
        expose(JpaTransactionHandler.class).annotatedWith(Names.named(name));
    }
}
