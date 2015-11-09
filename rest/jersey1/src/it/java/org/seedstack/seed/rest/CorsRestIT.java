/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;

import java.net.URL;

import static com.jayway.restassured.RestAssured.expect;

public class CorsRestIT extends AbstractSeedWebIT {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addAsResource("META-INF/configuration/org.seedstack.seed.rest.cors.props", "META-INF/configuration/org.seedstack.seed.rest.cors.props").setWebXML("WEB-INF/web.xml");
    }

    @RunAsClient
    @Test
    public void cors_is_applied_to_rest_resources_get(@ArquillianResource URL baseURL) {
        expect().statusCode(200).given().header("Origin", baseURL.toExternalForm()).get(baseURL.toString() + "cors");
    }

    @RunAsClient
    @Test
    public void cors_is_applied_to_rest_resources_post(@ArquillianResource URL baseURL) {
        expect().statusCode(200).given().header("Origin", baseURL.toExternalForm()).post(baseURL.toString() + "cors");
    }

    @RunAsClient
    @Test
    public void cors_is_applied_to_rest_resources_options(@ArquillianResource URL baseURL) {
        expect().statusCode(200).given().header("Origin", baseURL.toExternalForm()).options(baseURL.toString() + "cors");
    }

    @RunAsClient
    @Test
    public void cors_is_applied_to_rest_resources_head(@ArquillianResource URL baseURL) {
        expect().statusCode(200).given().header("Origin", baseURL.toExternalForm()).head(baseURL.toString() + "cors");
    }

    @RunAsClient
    @Test
    public void cors_is_applied_to_rest_resources_delete(@ArquillianResource URL baseURL) {
        expect().statusCode(200).given().header("Origin", baseURL.toExternalForm()).delete(baseURL.toString() + "cors");
    }

    @RunAsClient
    @Test
    public void cors_is_applied_to_rest_resources_put(@ArquillianResource URL baseURL) {
        expect().statusCode(200).given().header("Origin", baseURL.toExternalForm()).put(baseURL.toString() + "cors");
    }
}