/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal;


import com.google.inject.AbstractModule;
import org.seedstack.seed.ws.handlers.server.HttpBasicAuthenticationHandler;
import com.sun.xml.wss.RealmAuthenticationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

class WSModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(WSModule.class);

    private final Set<Class<?>> webServiceAnnotatedClasses;
    private final Set<Class<?>> webServiceclientClasses;
    private final Class<? extends RealmAuthenticationAdapter> realmAuthenticationAdapterClass;

    WSModule(Set<Class<?>> webServiceAnnotatedClassAndInterface, Set<Class<?>> webServiceclientClass, Class<? extends RealmAuthenticationAdapter> realmAuthenticationAdapterClass) {
        this.webServiceAnnotatedClasses = webServiceAnnotatedClassAndInterface;
        this.webServiceclientClasses = webServiceclientClass;
        this.realmAuthenticationAdapterClass = realmAuthenticationAdapterClass;
    }


    @Override
    protected void configure() {
        requestStaticInjection(SeedInstanceResolver.class);
        requestStaticInjection(HttpBasicAuthenticationHandler.class);
        requestStaticInjection(SeedRealmAuthenticationAdapterDelegate.class);

        bind(RealmAuthenticationAdapter.class).to(realmAuthenticationAdapterClass);

        for (Class<?> webServiceAnnotatedClass : webServiceAnnotatedClasses) {
            LOGGER.info("Binding web service " + webServiceAnnotatedClass.getSimpleName());
            bind(webServiceAnnotatedClass);
        }

        for (Class webServiceClientClazz : webServiceclientClasses) {
            LOGGER.info("Binding web service client " + webServiceClientClazz.getSimpleName());
            bind(webServiceClientClazz);
        }
    }
}
