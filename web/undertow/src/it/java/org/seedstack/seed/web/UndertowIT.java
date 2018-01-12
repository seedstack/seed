/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web;

import static com.jayway.restassured.RestAssured.expect;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.spi.SeedLauncher;

/**
 * Tests an undertow server exposing a simple hello world servlet and filter.
 */
public class UndertowIT {
    private final SeedLauncher launcher = Seed.getLauncher();

    @Before
    public void before() throws Exception {
        launcher.launch(new String[]{});
    }

    @After
    public void after() throws Exception {
        launcher.shutdown();
    }

    @Test
    public void test_run_seed_app_with_SSL() throws Exception {
        checkServer(9001);
    }

    @Test
    public void refresh() throws Exception {
        checkServer(9001);
        System.setProperty("customUndertowPort", "9002");
        launcher.refresh();
        checkServer(9002);
    }

    private void checkServer(int port) {
        RestAssured.useRelaxedHTTPSValidation();
        Response servletResponse = expect().statusCode(200).when().get("https://localhost:" + port + "/helloServlet");
        Assertions.assertThat(servletResponse.asString()).isEqualTo("Hello World! value1");
        Response filterResponse = expect().statusCode(200).when().get("https://localhost:" + port + "/helloFilter");
        Assertions.assertThat(filterResponse.asString()).isEqualTo("Hello World! value2");
    }
}
