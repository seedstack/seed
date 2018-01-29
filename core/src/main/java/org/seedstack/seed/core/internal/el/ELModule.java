/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.el;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import java.lang.annotation.Annotation;
import java.util.Map;
import javax.el.ExpressionFactory;
import org.seedstack.seed.el.ELContextBuilder;
import org.seedstack.seed.el.ELService;
import org.seedstack.seed.el.spi.ELHandler;

class ELModule extends AbstractModule {
    private final ExpressionFactory expressionFactory;
    private final Map<Class<? extends Annotation>, Class<ELHandler>> elMap;

    ELModule(ExpressionFactory expressionFactory, Map<Class<? extends Annotation>, Class<ELHandler>> elMap) {
        this.expressionFactory = expressionFactory;
        this.elMap = elMap;
    }

    @Override
    protected void configure() {
        bind(ExpressionFactory.class).toInstance(expressionFactory);
        bind(ELService.class).to(ELServiceInternal.class);
        bind(ELContextBuilder.class).to(ELContextBuilderImpl.class);

        for (Class<ELHandler> elHandlerClass : elMap.values()) {
            bind(elHandlerClass);
        }

        // bind the map of annotation -> ELHandler
        bind(new AnnotationHandlersTypeLiteral()).toInstance(ImmutableMap.copyOf(elMap));
    }

    private static class AnnotationHandlersTypeLiteral
            extends TypeLiteral<Map<Class<? extends Annotation>, Class<ELHandler>>> {
    }
}
