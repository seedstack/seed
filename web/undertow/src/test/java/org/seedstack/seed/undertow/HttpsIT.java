/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.undertow;

import io.restassured.RestAssured;
import io.restassured.config.SSLConfig;
import io.restassured.specification.ResponseSpecification;
import org.seedstack.seed.testing.ConfigurationProfiles;

@ConfigurationProfiles("https")
public class HttpsIT extends AbstractUndertowIT {
    ResponseSpecification expect() {
        return RestAssured.given()
                .config(RestAssured.config().sslConfig(SSLConfig.sslConfig().relaxedHTTPSValidation("SSL")))
                .expect();
    }
}
