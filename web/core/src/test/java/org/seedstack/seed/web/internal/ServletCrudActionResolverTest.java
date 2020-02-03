/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.security.CrudAction;

public class ServletCrudActionResolverTest {
    private ServletCrudActionResolver resolverUnderTest;

    @Before
    public void setup() {
        resolverUnderTest = new ServletCrudActionResolver();
    }

    @Test
    public void resolveRightVerb() throws Exception {
        assertThat(resolverUnderTest.resolve(Fixture.class.getDeclaredMethod("doGet", HttpServletRequest.class,
                HttpServletResponse.class))).isEqualTo(Optional.of(CrudAction.READ));
        assertThat(resolverUnderTest.resolve(Fixture.class.getDeclaredMethod("doHead", HttpServletRequest.class,
                HttpServletResponse.class))).isEqualTo(Optional.of(CrudAction.READ));
        assertThat(resolverUnderTest.resolve(Fixture.class.getDeclaredMethod("doPost", HttpServletRequest.class,
                HttpServletResponse.class))).isEqualTo(Optional.of(CrudAction.CREATE));
        assertThat(resolverUnderTest.resolve(Fixture.class.getDeclaredMethod("doPut", HttpServletRequest.class,
                HttpServletResponse.class))).isEqualTo(Optional.of(CrudAction.UPDATE));
        assertThat(resolverUnderTest.resolve(Fixture.class.getDeclaredMethod("doDelete", HttpServletRequest.class,
                HttpServletResponse.class))).isEqualTo(Optional.of(CrudAction.DELETE));
        assertThat(resolverUnderTest.resolve(Fixture.class.getDeclaredMethod("doOptions", HttpServletRequest.class,
                HttpServletResponse.class))).isEqualTo(Optional.of(CrudAction.READ));
        assertThat(resolverUnderTest.resolve(Fixture.class.getDeclaredMethod("doTrace", HttpServletRequest.class,
                HttpServletResponse.class))).isEqualTo(Optional.of(CrudAction.READ));
        assertThat(resolverUnderTest.resolve(Fixture.class.getMethod("doGet"))).isNotPresent();
    }

    // Test Fixture
    public static class Fixture extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doGet(req, resp);
        }

        @Override
        protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doHead(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doPost(req, resp);
        }

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doPut(req, resp);
        }

        @Override
        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doDelete(req, resp);
        }

        @Override
        protected void doOptions(HttpServletRequest req,
                HttpServletResponse resp) throws ServletException, IOException {
            super.doOptions(req, resp);
        }

        @Override
        protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doTrace(req, resp);
        }

        public void doGet() {

        }
    }
}
