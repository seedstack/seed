/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it.internal;

import com.google.inject.Injector;
import org.jboss.arquillian.test.spi.TestEnricher;

import javax.inject.Inject;
import java.lang.reflect.Method;

/**
 * Arquillian TestEnricher to enable SEED injection on tests.
 *
 * @author adrien.lauer@mpsa.com
 */
class InjectionEnricher implements TestEnricher {
    @Inject
    private static Injector injector;

    @Override
    public void enrich(Object testCase) {
        if (injector != null) {
            injector.injectMembers(testCase);
        }
    }

    @Override
    public Object[] resolve(Method method) {
        return new Object[method.getParameterTypes().length];
    }
}
