/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import io.restassured.RestAssured;
import java.net.URL;
import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JspIT {
    @ArquillianResource
    private URL baseUrl;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsWebInfResource("test.jsp", "jsp/test.jsp");
    }

    @Test
    @RunAsClient
    public void jspIsWorking() {
        String body = RestAssured.expect().statusCode(200).when().get(
                baseUrl + "jsp-test?test=value1").getBody().asString();
        Assertions.assertThat(body.trim()).isEqualTo("value1");
    }

    @Test
    @RunAsClient
    public void jspInclusionIsWorking() {
        String body = RestAssured.expect().statusCode(200).when().post(
                baseUrl + "jsp-test?test=value2").getBody().asString();
        Assertions.assertThat(body.trim()).isEqualTo("value2");
    }
}
