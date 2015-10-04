/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.metrics.internal;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.Method;

/**
 * A listener which adds method interceptors to metered methods.
 *
 * @author yves.dautremay@mpsa.com
 */
class MeteredListener implements TypeListener {
    private final MetricRegistry metricRegistry;

    MeteredListener(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public <T> void hear(TypeLiteral<T> literal,
                         TypeEncounter<T> encounter) {
        Class<? super T> klass = literal.getRawType();

        do {
            for (Method method : klass.getDeclaredMethods()) {
                final MethodInterceptor interceptor = MeteredInterceptor.forMethod(metricRegistry,
                        klass,
                        method);
                if (interceptor != null) {
                    encounter.bindInterceptor(Matchers.only(method), interceptor);
                }
            }
        } while ((klass = klass.getSuperclass()) != null);
    }
}
