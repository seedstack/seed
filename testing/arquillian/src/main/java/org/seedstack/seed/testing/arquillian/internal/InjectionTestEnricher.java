/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.arquillian.internal;

import com.google.inject.Injector;
import java.lang.reflect.Method;
import javax.inject.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Arquillian TestEnricher to enable SeedStack injection on tests.
 */
public class InjectionTestEnricher implements TestEnricher {
    private static final Logger LOGGER = LoggerFactory.getLogger(InjectionTestEnricher.class);
    @Inject
    private static Injector injector;

    @Override
    public void enrich(Object testCase) {
        if (injector != null) {
            injector.injectMembers(testCase);
        } else {
            LOGGER.warn("SeedStack injector is not available, cannot inject client-side Arquillian test {}",
                    testCase.getClass().getCanonicalName());
        }
    }

    @Override
    public Object[] resolve(Method method) {
        return new Object[method.getParameterTypes().length];
    }
}
