/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import java.util.Collection;
import java.util.HashMap;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Ignore;
import org.seedstack.seed.rest.RestConfig;

@RunWith(JMockit.class)
public class RestModuleTest {

    private RestModule underTest;
    @Mocked
    private RestConfig restConfig;

    @Before
    public void setUp() {
        Collection<Class<?>> resources = Lists.newArrayList(MyResource1.class, MyResource2.class);
        Collection<Class<?>> providers = Lists.newArrayList(MyProvider1.class, MyProvider2.class);
        underTest = new RestModule(restConfig, resources, providers, new HashMap<>());
    }

    @Test
    public void testBindResources(@Mocked final Binder binder) {
        underTest.configure(binder);
        new Verifications() {{
            binder.bind(MyResource1.class);
            binder.bind(MyResource2.class);
        }};
    }

    @Test
    public void testBindProviders(@Mocked final Binder binder) {
        underTest.configure(binder);
        new Verifications() {{
            binder.bind(MyProvider1.class).in(Scopes.SINGLETON);
            binder.bind(MyProvider2.class).in(Scopes.SINGLETON);
        }};
    }

    @Ignore
    @Path("/resource1")
    private static class MyResource1 {
    }

    @Ignore
    @Path("/resource2")
    private static class MyResource2 {
    }

    @Ignore
    @Provider
    private static class MyProvider1 {
    }

    @Ignore
    @Provider
    private static class MyProvider2 {
    }
}
