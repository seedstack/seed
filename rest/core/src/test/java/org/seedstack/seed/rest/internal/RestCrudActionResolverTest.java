/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.security.CrudAction;

public class RestCrudActionResolverTest {
    private RestCrudActionResolver resolverUnderTest;

    @Before
    public void setup() {
        resolverUnderTest = new RestCrudActionResolver();
    }

    @Test
    public void test_that_resolves_to_the_right_verb() throws Exception {
        assertThat(resolverUnderTest.resolve(Fixture.class.getMethod("delete"))).isPresent().contains(
                CrudAction.DELETE);
        assertThat(resolverUnderTest.resolve(Fixture.class.getMethod("get"))).isPresent().contains(CrudAction.READ);
        assertThat(resolverUnderTest.resolve(Fixture.class.getMethod("head"))).isPresent().contains(CrudAction.READ);
        assertThat(resolverUnderTest.resolve(Fixture.class.getMethod("options"))).isPresent().contains(CrudAction.READ);
        assertThat(resolverUnderTest.resolve(Fixture.class.getMethod("post"))).isPresent().contains(CrudAction.CREATE);
        assertThat(resolverUnderTest.resolve(Fixture.class.getMethod("put"))).isPresent().contains(CrudAction.UPDATE);
        assertThat(resolverUnderTest.resolve(Fixture.class.getMethod("none"))).isNotPresent();
        assertThat(resolverUnderTest.resolve(Fixture.class.getMethod("random"))).isNotPresent();
    }

    // Test Fixture
    public static class Fixture {

        @DELETE
        public void delete() {

        }

        @GET
        public void get() {

        }

        @Deprecated
        public void random() {

        }

        @HEAD
        public void head() {
        }

        @OPTIONS
        public void options() {

        }

        @POST
        public void post() {

        }

        @PUT
        public void put() {

        }

        public void none() {

        }

    }

}
