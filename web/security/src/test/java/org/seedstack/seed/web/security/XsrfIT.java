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

import io.restassured.response.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.LaunchWithUndertow;

@RunWith(SeedITRunner.class)
@LaunchWithUndertow
public class XsrfIT {
    private static final String XSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String XSRF_HEADER_NAME = "X-XSRF-TOKEN";
    private static final String SESSION_COOKIE_NAME = "JSESSIONID";
    @Configuration("web.runtime.baseUrl")
    private String baseUrl;

    @Test
    public void requestWithoutSessionShouldSucceed() {
        expect()
                .statusCode(200)
                .when()
                .get(baseUrl + "xsrf-protected-without-session");
    }

    @Test
    public void requestWithoutTokenShouldFail() {
        String sessionId = initiateSession().getCookie("JSESSIONID");
        given()
                .cookie(SESSION_COOKIE_NAME, sessionId)
                .expect()
                .statusCode(403)
                .when()
                .get(baseUrl + "xsrf-protected-with-session");
    }

    @Test
    public void requestWithCookieOnlyShouldFail() {
        Response response = initiateSession();
        String sessionId = response.getCookie(SESSION_COOKIE_NAME);
        String token = response.getCookie(XSRF_COOKIE_NAME);
        given()
                .cookie(SESSION_COOKIE_NAME, sessionId)
                .and()
                .cookie(XSRF_COOKIE_NAME, token)
                .expect()
                .statusCode(403)
                .when()
                .get(baseUrl + "xsrf-protected-with-session");
    }

    @Test
    public void requestWithHeaderOnlyShouldFail() {
        Response response = initiateSession();
        String sessionId = response.getCookie(SESSION_COOKIE_NAME);
        String token = response.getCookie(XSRF_COOKIE_NAME);
        given()
                .cookie(SESSION_COOKIE_NAME, sessionId)
                .and()
                .header(XSRF_HEADER_NAME, token)
                .expect()
                .statusCode(403)
                .when()
                .get(baseUrl + "xsrf-protected-with-session");
    }

    @Test
    public void requestWithCookieAndHeaderShouldSucceed() {
        Response response = initiateSession();
        String sessionId = response.getCookie(SESSION_COOKIE_NAME);
        String token = response.getCookie(XSRF_COOKIE_NAME);
        given()
                .cookie(SESSION_COOKIE_NAME, sessionId)
                .and()
                .cookie(XSRF_COOKIE_NAME, token)
                .and()
                .header(XSRF_HEADER_NAME, token)
                .expect()
                .statusCode(200)
                .when()
                .get(baseUrl + "xsrf-protected-with-session");
    }

    private Response initiateSession() {
        return given().auth()
                .basic("Obiwan", "yodarulez")
                .expect()
                .statusCode(200)
                .when()
                .get(baseUrl + "xsrf-protected-with-session");
    }
}
