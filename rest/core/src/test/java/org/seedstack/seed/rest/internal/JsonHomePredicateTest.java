/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.rest.Rel;

public class JsonHomePredicateTest {
    private JsonHomePredicate underTest = JsonHomePredicate.INSTANCE;

    @Test
    public void valid_json_entry_point_specification() throws NoSuchMethodException {
        Assertions.assertThat(underTest.test(ValidResource1.class.getMethod("post"))).isTrue();
        Assertions.assertThat(underTest.test(ValidResource2.class.getMethod("post"))).isTrue();

        Assertions.assertThat(underTest.test(InvalidResource1.class.getMethod("post"))).isFalse();
        Assertions.assertThat(underTest.test(InvalidResource2.class.getMethod("post"))).isFalse();
    }

    @Test
    @org.junit.Ignore
    public void valid_json_home_on_interface() throws NoSuchMethodException {
        Assertions.assertThat(underTest.test(ValidResource3.class.getMethod("post"))).isTrue();
    }

    static interface InterfaceResource1 {

        @Rel(value = "InterfaceResource1", home = true)
        @Path("/JsonHomeInterfaceResource1")
        @POST
        Response post();
    }

    static class ValidResource1 {

        @Rel(value = "ValidResource1", home = true)
        @Path("/JsonHomeValidResource1")
        @POST
        public Response post() {
            return null;
        }
    }

    static class ValidResource2 extends ValidResource1 {
    }

    static class ValidResource3 implements InterfaceResource1 {
        @Override
        @POST
        public Response post() {
            return null;
        }
    }

    static class InvalidResource1 {

        @Path("/JsonHomeInvalidResource1")
        @POST
        public Response post() {
            return null;
        }
    }

    @Path("/JsonHomeInvalidResource1")
    static class InvalidResource2 {
        public Response post() {
            return null;
        }
    }

}
