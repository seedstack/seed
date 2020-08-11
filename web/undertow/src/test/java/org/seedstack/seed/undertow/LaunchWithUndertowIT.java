/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.undertow;

import io.restassured.response.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.junit4.SeedITRunner;

import static io.restassured.RestAssured.expect;
import static org.assertj.core.api.Assertions.assertThat;

@LaunchWithUndertow
@RunWith(SeedITRunner.class)
public class LaunchWithUndertowIT {
    @Configuration("web.server.port")
    protected int configuredPort;
    @Configuration("runtime.web.server.port")
    protected int realPort;
    @Configuration("runtime.web.baseUrl")
    protected String baseUrl;

    @Test
    public void servlet() {
        assertThat(realPort).isEqualTo(configuredPort);
        Response servletResponse = expect()
                .statusCode(200)
                .when()
                .get(baseUrl + "/hello");
        assertThat(servletResponse.asString()).isEqualTo("Hello World!");
    }
}
