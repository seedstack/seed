/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.undertow;

import com.google.inject.Injector;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.spi.SeedLauncher;

/**
 * Tests an undertow server exposing a simple hello world servlet and filter.
 */
public class UndertowIT {
    private final SeedLauncher launcher = Seed.getLauncher();
    @Configuration("web.runtime.baseUrl")
    private String baseUrl;

    @BeforeClass
    public static void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Before
    public void before() throws Exception {
        launcher.launch(new String[]{});
        launcher.getKernel().ifPresent(k -> k.objectGraph().as(Injector.class).injectMembers(this));
    }

    @After
    public void after() throws Exception {
        launcher.shutdown();
    }

    @Test
    public void appRunningWithSSL() {
        checkServer();
    }

    @Test
    public void refresh() throws Exception {
        checkServer();
        System.setProperty("customUndertowPort", "9002");
        launcher.refresh();
        launcher.getKernel().ifPresent(k -> k.objectGraph().as(Injector.class).injectMembers(this));
        checkServer();
    }

    private void checkServer() {
        Response servletResponse = RestAssured.expect().statusCode(200).when().get(baseUrl + "hello");
        Assertions.assertThat(servletResponse.asString()).isEqualTo("Hello World!");
    }
}
