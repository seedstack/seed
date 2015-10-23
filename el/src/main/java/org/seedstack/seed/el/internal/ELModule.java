/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.el.internal;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import org.seedstack.seed.el.api.ELContextBuilder;
import org.seedstack.seed.el.api.ELService;
import org.seedstack.seed.el.spi.ELHandler;
import org.seedstack.seed.el.api.ELService;
import org.seedstack.seed.el.spi.ELHandler;

import java.lang.annotation.Annotation;
import java.util.Map;

class ELModule extends AbstractModule {
    private static final TypeLiteral<ImmutableMap<Class<? extends Annotation>, Class<ELHandler>>> MAP_TYPE_LITERAL = new TypeLiteral<ImmutableMap<Class<? extends Annotation>, Class<ELHandler>>>() {};
    private final Map<Class<? extends Annotation>, Class<ELHandler>> elMap;

    ELModule(Map<Class<? extends Annotation>, Class<ELHandler>> elMap) {
        this.elMap = elMap;
    }

    @Override
    protected void configure() {
        bind(ELService.class).to(ELServiceInternal.class);
        bind(ELContextBuilder.class).to(ELContextBuilderImpl.class);

        for (Class<ELHandler> elHandlerClass : elMap.values()) {
            bind(elHandlerClass);
        }

        // bind the map of annotation, elHandler
        bind(MAP_TYPE_LITERAL).toInstance(ImmutableMap.copyOf(elMap));
    }
}
