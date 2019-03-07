/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.undertow;

import com.google.inject.Injector;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.SSLConfig;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.spi.SeedLauncher;

public class RefreshIT {
    private static final String SEEDSTACK_PROFILES = "seedstack.profiles";
    private final SeedLauncher launcher = Seed.getLauncher();

    @Before
    public void setUp() throws Exception {
        System.setProperty(SEEDSTACK_PROFILES, "refresh");
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(SEEDSTACK_PROFILES);
    }

    @Test
    public void refresh() throws Exception {
        launcher.launch(new String[]{});
        launcher.getKernel().ifPresent(k -> k.objectGraph().as(Injector.class).injectMembers(this));

        checkServer(9001);

        System.setProperty("customUndertowPort", "9002");
        launcher.refresh();
        launcher.getKernel().ifPresent(k -> k.objectGraph().as(Injector.class).injectMembers(this));

        checkServer(9002);

        launcher.shutdown();
    }

    private void checkServer(int port) {
        Response servletResponse = expect()
                .statusCode(200)
                .when()
                .get(String.format("http://localhost:%d/hello", port));
        Assertions.assertThat(servletResponse.asString()).isEqualTo("Hello World!");
    }

    private ResponseSpecification expect() {
        return RestAssured.given()
                .config(RestAssured.config().sslConfig(SSLConfig
                        .sslConfig().relaxedHTTPSValidation("SSL"))
                        .httpClient(HttpClientConfig.httpClientConfig()
                                .setParam("CoreConnectionPNames.SO_TIMEOUT", 1000))
                )
                .expect();
    }
}
