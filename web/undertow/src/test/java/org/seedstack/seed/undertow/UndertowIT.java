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
import io.restassured.config.SSLConfig;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.spi.SeedLauncher;

/**
 * Tests an undertow server exposing a simple hello world servlet and filter.
 */
public class UndertowIT {
    private final SeedLauncher launcher = Seed.getLauncher();
    @Configuration("runtime.web.baseUrl")
    private String baseUrl;

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

    @Test
    public void staticResource() {
        expect()
                .statusCode(200)
                .body(Matchers.containsString("<h1>Hello</h1>"))
                .when()
                .get(baseUrl + "index.html");
    }

    private void checkServer() {
        Response servletResponse = expect()
                .statusCode(200)
                .when()
                .get(baseUrl + "hello");
        Assertions.assertThat(servletResponse.asString()).isEqualTo("Hello World!");
    }

    private ResponseSpecification expect() {
        return RestAssured.given()
                .config(RestAssured.config().sslConfig(SSLConfig.sslConfig().relaxedHTTPSValidation("SSL")))
                .expect();
    }
}
