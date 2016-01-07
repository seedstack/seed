/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.internal;

import com.google.inject.Injector;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@RunWith(JMockit.class)
public class GuiceToHK2FactoryTest {

    final SomeClass someObject = new SomeClass();
    private GuiceToHK2Factory underTest;
    @Mocked
    private Injector injector;
    @Mocked
    private ServiceLocator serviceLocator;

    @Before
    public void setUp() throws Exception {
        underTest = new GuiceToHK2Factory(SomeClass.class, injector, serviceLocator);
    }

    @Test
    public void testProvide() throws Exception {
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
    public void testProvideNull() throws Exception {
        new Expectations() {{
            injector.getInstance(SomeClass.class); result = null;
        }};

        Object providedObject = underTest.provide();

        assertThat(providedObject).isNull();
    }

    class SomeClass {
    }
}
