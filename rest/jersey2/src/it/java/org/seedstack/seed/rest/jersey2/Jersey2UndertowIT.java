/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2;

import com.jayway.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seedstack.seed.core.SeedMain;
import org.seedstack.seed.spi.SeedLauncher;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.ws.rs.core.MediaType;

import static com.jayway.restassured.RestAssured.expect;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class Jersey2UndertowIT {

    private SeedLauncher launcher;

    @Before
    public void setUp() throws Exception {
        launcher = SeedMain.getLauncher();
        launcher.launch(new String[]{});
    }

    @Test
    public void jersey2_is_working_with_undertow() throws Exception {
        Response response = expect().statusCode(200).given().contentType(MediaType.APPLICATION_JSON)
                .body("{ \"body\": \"hello world!\", \"author\": \"test\" }")
                .post("http://localhost:8080/message");

        JSONAssert.assertEquals(response.asString(), "{\"body\":\"test says: hello world!\",\"author\":\"computer\"}", true);
    }

    @After
    public void tearDown() throws Exception {
        launcher.shutdown();
    }

}
