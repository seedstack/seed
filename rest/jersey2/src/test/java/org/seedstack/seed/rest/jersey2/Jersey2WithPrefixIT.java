/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2;

import static io.restassured.RestAssured.expect;

import javax.ws.rs.core.MediaType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.SystemProperty;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.LaunchWithUndertow;

@RunWith(SeedITRunner.class)
@LaunchWithUndertow
@SystemProperty(name = "seedstack.profiles", value = "withPrefix")
public class Jersey2WithPrefixIT {
    @Configuration("runtime.rest.baseUrl")
    private String baseUrl;

    @Test
    public void restPrefixIsHonored() {
        expect().statusCode(200).given().contentType(MediaType.APPLICATION_JSON).get(baseUrl + "/hello");
    }
}
