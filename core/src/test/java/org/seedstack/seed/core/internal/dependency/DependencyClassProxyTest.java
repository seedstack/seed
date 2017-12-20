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

public class DependencyClassProxyTest {
    @Test
    public void testInvoke() {
        final int result = 10;
        AbstractDummyProxy proxy = new DependencyClassProxy<>(AbstractDummyProxy.class, new Object() {
            public int getResult() {
                return result;
            }
        }).getProxy();
        Assertions.assertThat(proxy.getResult()).isEqualTo(result);
    }

    @Test
    public void testInvokeWithException() {
        final String errorMessage = "dummy exception";
        AbstractDummyProxy proxy = new DependencyClassProxy<>(AbstractDummyProxy.class, new Object() {
            public int getResult() {
                throw new RuntimeException(errorMessage);
            }
        }).getProxy();
        try {
            proxy.getResult();
        } catch (Exception e) {
            Assertions.assertThat(e).isInstanceOf(RuntimeException.class);
            Assertions.assertThat(e.getMessage()).isEqualTo(errorMessage);
        }
    }

    @Test(expected = SeedException.class)
    public void testCreationError() {
        new DependencyClassProxy<>(AbstractDummyProxyError.class, new Object() {
        });
    }

    static abstract class AbstractDummyProxy {
        protected abstract int getResult();
    }

    abstract class AbstractDummyProxyError {
    }
}
