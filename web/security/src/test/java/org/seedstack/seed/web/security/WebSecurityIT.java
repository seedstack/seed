/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.config.SSLConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.security.KeyStore;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.LaunchWithUndertow;

@RunWith(SeedITRunner.class)
@LaunchWithUndertow
public class WebSecurityIT {
    @Inject
    @Named("master")
    private KeyStore keyStore;
    @Configuration("web.runtime.baseUrl")
    private String baseUrl;

    @After
    public void tearDown() {
        givenRelaxedSSL().expect().statusCode(200).when().get(baseUrl + "logout");
    }

    @Test
    public void requestOnSecuredResourceShouldSend401() {
        givenRelaxedSSL().expect().statusCode(401).when().get(baseUrl + "jediCouncil.html");
    }

    @Test
    public void requestOnSecuredResourceWithGoodBasicauthShouldSend200OnAuthorizedResource() {
        givenRelaxedSSL().auth().basic("Obiwan", "yodarulez").expect().statusCode(200).when().get(
                baseUrl + "jediCouncil.html");
    }

    @Test
    public void requestOnSecuredResourceWithGoodBasicauthShouldSend401OnForbiddenResource() {
        givenRelaxedSSL().auth().basic("Anakin", "imsodark").expect().statusCode(401).when().get(
                baseUrl + "jediCouncil.html");
    }

    @Test
    public void requestOnAnonymousResourceShouldSend200() {
        givenRelaxedSSL().expect().statusCode(200).when().get(baseUrl + "image.jpg");
    }

    @Test
    public void responseShouldBeATeapotWhenRequestingUrlTeapot() {
        givenRelaxedSSL().given().expect().statusCode(418).when().get(baseUrl + "teapot");
    }

    @Test
    public void logoutShouldRedirect() {
        assertThat(givenRelaxedSSL().expect()
                .statusCode(200)
                .when()
                .get(baseUrl + "logout")
                .body()
                .asString()).contains(
                "You are logged out!");
    }

    @Test
    public void authcShouldRedirectToLogin() {
        assertThat(givenRelaxedSSL().expect().statusCode(200).when().get(baseUrl + "protected").body().asString())
                .contains("Please login:");
    }

    @Test
    @Ignore
    public void requestWithCertificateShouldSend200() {
        givenRelaxedSSL().expect().statusCode(200).when().get(baseUrl + "cert-protected");
    }

    @Test
    public void loginSuccessShouldRedirect() {
        assertThat(givenRelaxedSSL()
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

    private RequestSpecification givenRelaxedSSL() {
        return RestAssured.given()
                .config(RestAssured.config().sslConfig(SSLConfig.sslConfig().relaxedHTTPSValidation("SSL")));
    }
}
