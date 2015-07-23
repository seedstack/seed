/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
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
import org.json.JSONException;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;

import java.net.URL;

import static com.jayway.restassured.RestAssured.expect;

public class ResourceTemplateIT extends AbstractSeedWebIT {
    @ArquillianResource
    URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).setWebXML("WEB-INF/web.xml");
    }

    @RunAsClient
    @Test
    public void template_resources_are_working() throws JSONException {
        expect().statusCode(200).given().header("Accept", "application/json").get(baseURL + "lists/persons");
        expect().statusCode(200).given().header("Accept", "application/json").get(baseURL + "lists/persons/0");

        expect().statusCode(200).given().header("Accept", "application/json").get(baseURL + "lists/products");
        expect().statusCode(200).given().header("Accept", "application/json").get(baseURL + "lists/products/0");
    }
}