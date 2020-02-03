/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Injector;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.Before;
import org.junit.Test;

public class GuiceToHK2FactoryTest {
    private final SomeClass someObject = new SomeClass();
    private GuiceToHK2Factory<SomeClass> underTest;
    @Mocked
    private Injector injector;
    @Mocked
    private ServiceLocator serviceLocator;

    @Before
    public void setUp() {
        underTest = new GuiceToHK2Factory<>(SomeClass.class, injector, serviceLocator);
    }

    @Test
    public void testProvide() {
        new Expectations() {{
            injector.getInstance(SomeClass.class);
            result = someObject;
        }};

        Object providedObject = underTest.provide();

        assertThat(providedObject).isEqualTo(someObject);
        new Verifications() {{
            serviceLocator.inject(someObject);
        }};
    }

    @Test
    public void testProvideNull() {
        new Expectations() {{
            injector.getInstance(SomeClass.class);
            result = null;
        }};

        Object providedObject = underTest.provide();

        assertThat(providedObject).isNull();
    }

    class SomeClass {
    }
}
