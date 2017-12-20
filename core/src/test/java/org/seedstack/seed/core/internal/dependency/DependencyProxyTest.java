/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.dependency;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.spi.DependencyProvider;

public class DependencyProxyTest {
    private static final String PROXY_METHOD = "proxy method";

    @Test
    public void testInvoke() {
        DependencyProvider provider = new DependencyProxy<DependencyProvider>(new Class[]{DependencyProvider.class},
                new Object() {
                    @SuppressWarnings("unused")
                    public String getClassToCheck() {
                        return PROXY_METHOD;
                    }
                }).getProxy();
        Assertions.assertThat(provider.getClassToCheck()).isEqualTo(PROXY_METHOD);
    }

    @Test(expected = SeedException.class)
    public void testCreationError() {
        new DependencyProxy<DependencyProvider>(new Class[]{String.class}, new Object() {
        });
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testInvokeWithoutMethod() {
        DependencyProvider provider = new DependencyProxy<DependencyProvider>(new Class[]{DependencyProvider.class},
                new Object() {
                }).getProxy();
        provider.getClassToCheck();
    }

    @Test
    public void testInvokeWithoutInvocationError() {
        final String errorMessage = "Dummy exception";
        DependencyProvider provider = new DependencyProxy<DependencyProvider>(new Class[]{DependencyProvider.class},
                new Object() {
                    @SuppressWarnings("unused")
                    public String getClassToCheck() {
                        throw new RuntimeException(errorMessage);
                    }
                }).getProxy();
        try {
            provider.getClassToCheck();
        } catch (Exception e) {
            Assertions.assertThat(e).isInstanceOf(RuntimeException.class);
            Assertions.assertThat(e.getMessage()).isEqualTo(errorMessage);
        }
    }

}
