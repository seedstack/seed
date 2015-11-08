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

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class ErroneousResourceIT extends AbstractSeedWebIT {

    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).setWebXML("WEB-INF/web.xml");
    }

    @RunAsClient
    @Test
    public void test_map_all_exception() {
        expect().statusCode(500).given().get(baseURL.toString() + "error/internal");
    }

    @RunAsClient
    @Test
    public void test_map_authentication_exception() {
        expect().statusCode(401).given().get(baseURL.toString() + "error/authentication");
    }

    @RunAsClient
    @Test
    public void test_map_authorization_exception() {
        expect().statusCode(403).given().get(baseURL.toString() + "error/authorization");
    }

    @RunAsClient
    @Test
    public void test_map_web_application_exception() {
        expect().statusCode(404).given().get(baseURL.toString() + "error/notFound");
    }
}
