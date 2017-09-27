/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web;

import static com.jayway.restassured.RestAssured.given;

import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;

public class CorsIT extends AbstractSeedWebIT {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class)
                .addAsResource("configuration/cors.yaml", "META-INF/configuration/cors.yaml");
    }

    @Test
    @RunAsClient
    public void cors_support_is_enabled_for_get(@ArquillianResource URL baseURL) throws Exception {
        given().header("Origin", baseURL.toExternalForm()).expect().statusCode(200).header(
                "Access-Control-Allow-Origin", baseURL.toString()).when().get(baseURL.toString() + "cors");
    }

    @Test
    @RunAsClient
    public void cors_support_is_enabled_for_post(@ArquillianResource URL baseURL) throws Exception {
        given().header("Origin", baseURL.toExternalForm()).expect().statusCode(200).header(
                "Access-Control-Allow-Origin", baseURL.toString()).when().post(baseURL.toString() + "cors");
    }

    @Test
    @RunAsClient
    public void cors_support_is_enabled_for_put(@ArquillianResource URL baseURL) throws Exception {
        given().header("Origin", baseURL.toExternalForm()).expect().statusCode(200).header(
                "Access-Control-Allow-Origin", baseURL.toString()).when().put(baseURL.toString() + "cors");
    }

    @Test
    @RunAsClient
    public void cors_support_is_enabled_for_delete(@ArquillianResource URL baseURL) throws Exception {
        given().header("Origin", baseURL.toExternalForm()).expect().statusCode(200).header(
                "Access-Control-Allow-Origin", baseURL.toString()).when().delete(baseURL.toString() + "cors");
    }

    @Test
    @RunAsClient
    public void cors_support_is_enabled_for_head(@ArquillianResource URL baseURL) throws Exception {
        given().header("Origin", baseURL.toExternalForm()).expect().statusCode(200).header(
                "Access-Control-Allow-Origin", baseURL.toString()).when().head(baseURL.toString() + "cors");
    }

    @Test
    @RunAsClient
    public void cors_support_is_enabled_for_options(@ArquillianResource URL baseURL) throws Exception {
        given().header("Origin", baseURL.toExternalForm()).expect().statusCode(200).header(
                "Access-Control-Allow-Origin", baseURL.toString()).when().options(baseURL.toString() + "cors");
    }
}
