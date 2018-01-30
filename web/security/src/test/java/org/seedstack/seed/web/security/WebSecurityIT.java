/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.security;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.LaunchWithUndertow;

@RunWith(SeedITRunner.class)
@LaunchWithUndertow
public class WebSecurityIT {
    @Configuration("web.runtime.baseUrl")
    private String baseUrl;

    @After
    public void tearDown() {
        expect().statusCode(200).when().get(baseUrl + "logout");
    }

    @Test
    public void requestOnSecuredResourceShouldSend401() {
        expect().statusCode(401).when().get(baseUrl + "jediCouncil.html");
    }

    @Test
    public void requestOnSecuredResourceWithGoodBasicauthShouldSend200OnAuthorizedResource() {
        given().auth().basic("Obiwan", "yodarulez").expect().statusCode(200).when().get(
                baseUrl + "jediCouncil.html");
    }

    @Test
    public void requestOnSecuredResourceWithGoodBasicauthShouldSend401OnForbiddenResource() {
        given().auth().basic("Anakin", "imsodark").expect().statusCode(401).when().get(
                baseUrl + "jediCouncil.html");
    }

    @Test
    public void requestOnAnonymousResourceShouldSend200() {
        expect().statusCode(200).when().get(baseUrl + "image.jpg");
    }

    @Test
    public void responseShouldBeATeapotWhenRequestingUrlTeapot() {
        expect().statusCode(418).when().get(baseUrl + "teapot");
    }

    @Test
    public void logoutShouldRedirect() {
        Assertions.assertThat(expect().statusCode(200).when().get(baseUrl + "logout").body().asString()).contains(
                "You are logged out!");
    }

    @Test
    public void authcShouldRedirectToLogin() {
        Assertions.assertThat(expect().statusCode(200).when().get(baseUrl + "protected").body().asString())
                .contains(
                        "Please login:");
    }

    @Test
    public void loginSuccessShouldRedirect() {
        Assertions.assertThat(given()
                .contentType(ContentType.URLENC)
                .formParam("user", "Obiwan")
                .formParam("pw", "yodarulez")
                .expect()
                .statusCode(302)
                .when()
                .post(baseUrl + "login.html")
                .header("Location"))
                .endsWith("/success.html");
    }
}
