/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.it;

import com.google.inject.AbstractModule;
import java.util.Set;
import org.seedstack.seed.testing.spi.TestDecorator;

class ITModule extends AbstractModule {
    private final Class<?> testClass;
    private final Set<Class<? extends TestDecorator>> testDecorators;

    ITModule(Class<?> testClass,
            Set<Class<? extends TestDecorator>> testDecorators) {
        this.testClass = testClass;
        this.testDecorators = testDecorators;
    }

    @Override
    protected void configure() {
        if (testClass != null) {
            bind(testClass);
        }
        for (Class<? extends TestDecorator> testDecorator : testDecorators) {
            bind(testDecorator);
        }
    }
}
