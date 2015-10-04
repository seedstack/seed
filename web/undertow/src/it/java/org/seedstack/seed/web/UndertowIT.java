/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.core.SeedMain;

/**
 * Tests an undertow server exposing a simple hello world servlet.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class UndertowIT {

    @Test
    public void test_run_seed_app_with_SSL() throws InterruptedException {
        SeedMain.main(null);
        RestAssured.useRelaxedHTTPSValidation();
        Response response = RestAssured.expect().statusCode(200).when().get("https://localhost:9001/hello");
        Assertions.assertThat(response.asString()).isEqualTo("Hello World! value1");
    }
}
