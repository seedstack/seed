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
import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class XsrfIT {
    private static final String XSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String XSRF_HEADER_NAME = "X-XSRF-TOKEN";
    private static final String SESSION_COOKIE_NAME = "JSESSIONID";
    @ArquillianResource
    private URL baseUrl;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Test
    @RunAsClient
    public void requestWithoutSessionShouldSucceed() {
        expect()
                .statusCode(200)
                .when()
                .get(baseUrl + "xsrf-protected-without-session");
    }

    @Test
    @RunAsClient
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
    @RunAsClient
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
    @RunAsClient
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
    @RunAsClient
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
