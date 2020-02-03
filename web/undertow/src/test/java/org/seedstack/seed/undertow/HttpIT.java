/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.undertow;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class HttpIT extends AbstractUndertowIT {
    ResponseSpecification expect() {
        return RestAssured.expect();
    }

    @Test
    public void servlet() {
        Response servletResponse = expect()
                .statusCode(200)
                .when()
                .get(baseUrl + "/hello");
        Assertions.assertThat(servletResponse.asString()).isEqualTo("Hello World!");
    }
}
