/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.core.SeedMain;
import org.seedstack.seed.spi.SeedLauncher;

/**
 * Tests an undertow server exposing a simple hello world servlet.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class UndertowIT {
    private final SeedLauncher launcher = SeedMain.getLauncher();

    @Before
    public void before() throws Exception {
        launcher.launch(new String[]{});
    }

    @After
    public void after() throws Exception {
        launcher.shutdown();
    }

    @Test
    public void test_run_seed_app_with_SSL() throws InterruptedException {
        RestAssured.useRelaxedHTTPSValidation();
        Response response = RestAssured.expect().statusCode(200).when().get("https://localhost:9001/hello");
        Assertions.assertThat(response.asString()).isEqualTo("Hello World! value1");
    }
}
