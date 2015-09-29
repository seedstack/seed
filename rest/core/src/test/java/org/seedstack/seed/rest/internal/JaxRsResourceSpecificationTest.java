/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import io.nuun.kernel.api.annotations.Ignore;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@Ignore // Tells nuun to not scan the test class
public class JaxRsResourceSpecificationTest {

    private JaxRsResourceSpecification underTest = new JaxRsResourceSpecification();

    @Test
    public void valid_resource_specification() {
        Assertions.assertThat(underTest.isSatisfiedBy(ValidResource1.class)).isTrue();
        Assertions.assertThat(underTest.isSatisfiedBy(ValidResource2.class)).isTrue();
        Assertions.assertThat(underTest.isSatisfiedBy(ValidResource3.class)).isTrue();
        Assertions.assertThat(underTest.isSatisfiedBy(ValidResource4.class)).isTrue();

        Assertions.assertThat(underTest.isSatisfiedBy(InvalidResource1.class)).isFalse();
        Assertions.assertThat(underTest.isSatisfiedBy(InvalidResource2.class)).isFalse();
        Assertions.assertThat(underTest.isSatisfiedBy(InvalidResource3.class)).isFalse();
    }

    @Path("/JaxValidResource1")
    static class ValidResource1 {}


    static class ValidResource2 {

        @Path("/JaxValidResource2")
        public Response post() {return null;}
    }

    static interface InterfaceResource {

        @Path("/JaxInterfaceResource")
        Response post();
    }

    static class ValidResource3 extends ValidResource2 {} // method annotations are inherited
    static class ValidResource4 implements InterfaceResource {
        @Override
        public Response post() {
            return null;
        }
    }

    static class InvalidResource1 {}

    @Path("/crud")
    static abstract class InvalidResource2 {}

    static class InvalidResource3 extends ValidResource1 {} // class annotations are not inherited
}
