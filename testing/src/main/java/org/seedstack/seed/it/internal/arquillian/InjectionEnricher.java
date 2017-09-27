/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.it.internal.arquillian;

import com.google.inject.Injector;
import java.lang.reflect.Method;
import javax.inject.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Arquillian TestEnricher to enable SEED injection on tests.
 */
public class InjectionEnricher implements TestEnricher {
    @Inject
    private static Injector injector;

    private Logger logger = LoggerFactory.getLogger(InjectionEnricher.class);

    @Override
    public void enrich(Object testCase) {
        if (injector != null) {
            injector.injectMembers(testCase);
        } else {
            logger.warn("Seed injector is not available, cannot inject Arquillian test {}",
                    testCase.getClass().getCanonicalName());
        }
    }

    @Override
    public Object[] resolve(Method method) {
        return new Object[method.getParameterTypes().length];
    }
}
