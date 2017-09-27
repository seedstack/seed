/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web;

import static com.jayway.restassured.RestAssured.expect;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;

public class JspIT extends AbstractSeedWebIT {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap
                .create(WebArchive.class)
                .addAsWebInfResource("test.jsp", "jsp/test.jsp");
    }

    @Test
    @RunAsClient
    public void jsp_is_working(@ArquillianResource URL baseURL) throws Exception {
        String body = expect().statusCode(200).when().get(
                baseURL.toString() + "jsp-test?test=value1").getBody().asString();
        assertThat(body.trim()).isEqualTo("value1");
    }

    @Test
    @RunAsClient
    public void jsp_inclusion_is_working(@ArquillianResource URL baseURL) throws Exception {
        String body = expect().statusCode(200).when().post(
                baseURL.toString() + "jsp-test?test=value2").getBody().asString();
        assertThat(body.trim()).isEqualTo("value2");
    }
}
