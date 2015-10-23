/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import org.seedstack.seed.it.AbstractSeedWebIT;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import java.net.URL;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;

public class ServletAndFilterRegistrationIT extends AbstractSeedWebIT {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).setWebXML("WEB-INF/web.xml");
    }

    @Test
    @RunAsClient
    public void servlet_is_correctly_registered(@ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).body(startsWith(TestServlet.CONTENT)).when().get(baseURL.toString() + "testServlet1");
        expect().statusCode(200).body(startsWith(TestServlet.CONTENT)).when().get(baseURL.toString() + "testServlet2");
    }

    @Test
    @RunAsClient
    public void servlet_params_are_correctly_initialized(@ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).body(endsWith(TestServlet.PARAM1_VALUE)).when().get(baseURL.toString() + "testServlet1");
        expect().statusCode(200).body(endsWith(TestServlet.PARAM1_VALUE)).when().get(baseURL.toString() + "testServlet2");
    }


    @Test
    @RunAsClient
    public void filter_is_correctly_registered(@ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).body(startsWith(TestFilter.CONTENT)).when().get(baseURL.toString() + "testFilter1");
        expect().statusCode(200).body(startsWith(TestFilter.CONTENT)).when().get(baseURL.toString() + "testFilter2");
    }

    @Test
    @RunAsClient
    public void filter_params_are_correctly_initialized(@ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(200).body(endsWith(TestFilter.PARAM1_VALUE)).when().get(baseURL.toString() + "testFilter1");
        expect().statusCode(200).body(endsWith(TestFilter.PARAM1_VALUE)).when().get(baseURL.toString() + "testFilter1");
    }
}
