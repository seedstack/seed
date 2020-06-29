/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import io.nuun.kernel.api.annotations.Ignore;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.assertj.core.api.Assertions;
import org.junit.Test;

@Ignore // Tells nuun to not scan the test class
public class JaxRsResourcePredicateTest {
    private JaxRsResourcePredicate underTest = JaxRsResourcePredicate.INSTANCE;

    @Test
    public void valid_resource_specification() {
        Assertions.assertThat(underTest.test(AbstractResource.class)).isFalse();

        Assertions.assertThat(underTest.test(ValidResource1.class)).isTrue();
        Assertions.assertThat(underTest.test(ValidResource2.class)).isTrue();
        Assertions.assertThat(underTest.test(ValidResource3.class)).isTrue();

        Assertions.assertThat(underTest.test(InvalidResource1.class)).isFalse();
        Assertions.assertThat(underTest.test(InvalidResource2.class)).isFalse();
        Assertions.assertThat(underTest.test(InvalidResource3.class)).isFalse();
        Assertions.assertThat(underTest.test(InvalidResource4.class)).isFalse();
    }

    interface InterfaceResource {

        @Path("/JaxInterfaceResource")
        Response post();
    }

    @Path("/JaxValidResource1")
    static class ValidResource1 {
    }

    static abstract class AbstractResource {
        @Path("/JaxValidResource2")
        public Response post() {
            return null;
        }
    }

    @Path("/prefix")
    static class ValidResource2 extends AbstractResource {
    }

    @Path("/prefix")
    static class ValidResource3 implements InterfaceResource {
        @Override
        public Response post() {
            return null;
        }
    }

    static class InvalidResource1 {
    }

    @Path("/crud")
    static abstract class InvalidResource2 {
    }

    static class InvalidResource3 extends ValidResource1 {
    } // class annotations are not inherited

    static class InvalidResource4 { //
        @Path("/JaxInvalidResource4")
        public Response post() {
            return null;
        }
    }
}
