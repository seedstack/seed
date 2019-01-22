/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.internal;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import org.seedstack.seed.testing.Expected;
import org.seedstack.seed.testing.spi.TestContext;
import org.seedstack.seed.testing.spi.TestPlugin;
import org.seedstack.shed.reflect.Annotations;

public class ExpectedTestPlugin implements TestPlugin {
    @Override
    public boolean enabled(TestContext testContext) {
        return true;
    }

    @Override
    public Optional<Class<? extends Exception>> expectedException(TestContext testContext) {
        return testContext.testMethod().map(this::findExpected).orElseGet(() -> findExpected(testContext.testClass()));
    }

    private Optional<Class<? extends Exception>> findExpected(AnnotatedElement annotatedElement) {
        return Annotations.on(annotatedElement)
                .includingMetaAnnotations()
                .find(Expected.class)
                .filter(expected -> !Expected.None.class.equals(expected.value()))
                .map(Expected::value);
    }
}
