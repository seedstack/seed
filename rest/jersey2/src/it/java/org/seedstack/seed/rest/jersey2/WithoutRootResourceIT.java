/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.jersey2;

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

public class WithoutRootResourceIT extends AbstractSeedWebIT {

    @ArquillianResource
    URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addAsResource("without-root-resource.yaml",
                "META-INF/configuration/without-root-resource.yaml").addAsWebResource("index.html", "index.html");
    }

    @RunAsClient
    @Test
    public void no_root_resource_is_served() {
        String response1 = expect().statusCode(200).given()
                .header("Accept", "*/*")
                .get(baseURL.toString()).asString();

        assertThat(response1).contains("<h1>Index!</h1>");
    }

}