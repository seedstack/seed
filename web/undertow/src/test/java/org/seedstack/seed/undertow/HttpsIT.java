/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.undertow;

import io.restassured.RestAssured;
import io.restassured.config.RedirectConfig;
import io.restassured.config.SSLConfig;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.testing.ConfigurationProfiles;

@ConfigurationProfiles("https")
public class HttpsIT extends AbstractUndertowIT {
    ResponseSpecification expect() {
        return expect(true);
    }

    private ResponseSpecification expect(boolean followRedirects) {
        return RestAssured.given()
                .config(RestAssured.config()
                        .sslConfig(SSLConfig.sslConfig().relaxedHTTPSValidation("TLS"))
                        .redirect(RedirectConfig.redirectConfig().followRedirects(followRedirects))
                )
                .expect();
    }

    @Test
    public void servlet() {
        Response servletResponse = expect()
                .statusCode(200)
                .when()
                .get(baseUrl + "/hello");
        Assertions.assertThat(servletResponse.asString()).isEqualTo("Hello World (secure)!");
    }

    @Test
    public void httpIsRedirectedToHttps() {
        expect(false)
                .statusCode(302)
                .header("Location", baseUrl + "/hello")
                .when()
                .get(baseUrl.replace("https://", "http://").replace(":8443", ":8080") + "/hello");
    }
}
