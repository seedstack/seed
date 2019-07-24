/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.undertow;

import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.ConfigurationProfiles;
import org.seedstack.seed.testing.LaunchMode;
import org.seedstack.seed.testing.LaunchWith;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.internal.UndertowLauncher;

/**
 * Tests an undertow server exposing a simple hello world servlet and filter.
 */
@LaunchWith(value = UndertowLauncher.class, mode = LaunchMode.PER_TEST)
@RunWith(SeedITRunner.class)
public abstract class AbstractUndertowIT {
    @Configuration("runtime.web.baseUrl")
    private String baseUrl;

    @Test
    public void servlet() {
        Response servletResponse = expect()
                .statusCode(200)
                .when()
                .get(baseUrl + "/hello");
        Assertions.assertThat(servletResponse.asString()).isEqualTo("Hello World!");
    }

    @Test
    public void staticResource() {
        expect()
                .statusCode(200)
                .body(Matchers.containsString("<h1>Hello</h1>"))
                .when()
                .get(baseUrl + "/index.html");
    }

    @Test
    public void defaultWelcomeFiles() {
        expect()
                .statusCode(200)
                .body(Matchers.containsString("<h1>Hello</h1>"))
                .when()
                .get(baseUrl + "/");
        expect()
                .statusCode(200)
                .body(Matchers.containsString("<h1>Hello</h1>"))
                .when()
                .get(baseUrl);
    }

    @Test
    @ConfigurationProfiles("welcome")
    public void explicitWelcomeFiles() {
        expect()
                .statusCode(200)
                .body(Matchers.containsString("<h1>Welcome1</h1>"))
                .when()
                .get(baseUrl + "/");
        expect()
                .statusCode(200)
                .body(Matchers.containsString("<h1>Welcome1</h1>"))
                .when()
                .get(baseUrl);
    }

    @Test
    @ConfigurationProfiles("welcome")
    public void explicitSubdirWelcomeFiles() {
        expect()
                .statusCode(200)
                .body(Matchers.containsString("<h1>Welcome2</h1>"))
                .when()
                .get(baseUrl + "/welcome/");
        expect()
                .statusCode(200)
                .body(Matchers.containsString("<h1>Welcome2</h1>"))
                .when()
                .get(baseUrl + "/welcome");
    }

    @Test
    @ConfigurationProfiles("errorPages")
    public void errorPages() {
        expect()
                .statusCode(404)
                .body(Matchers.containsString("<h1>Not found!</h1>"))
                .when()
                .get(baseUrl + "/error?code=404");
        expect()
                .statusCode(415)
                .body(Matchers.containsString("<h1>Unsupported media type!</h1>"))
                .when()
                .get(baseUrl + "/error?code=415");
        expect()
                .statusCode(403)
                .body(Matchers.containsString("<h1>An error occurred!</h1>"))
                .when()
                .get(baseUrl + "/error?code=403");
        expect()
                .statusCode(500)
                .body(Matchers.containsString("<h1>An error occurred!</h1>"))
                .when()
                .get(baseUrl + "/exceptionError");
    }

    abstract ResponseSpecification expect();
}
