/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.diagnostic.DiagnosticManager;
import org.seedstack.seed.it.AbstractSeedWebIT;

public class WebIT extends AbstractSeedWebIT {
    @Inject
    private DiagnosticManager diagnosticManager;
    @Configuration("web.runtime.servlet.contextPath")
    private String contextPath;
    @ArquillianResource
    private URL baseUrl;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Test
    @Ignore("remote injection not working, please fix if possible")
    public void testIsInjected() throws Exception {
        assertThat(diagnosticManager).isNotNull();
        assertThat(diagnosticManager).isNotNull();
    }

    @Test
    @RunAsClient
    public void testAsClientIsInjected() throws Exception {
        assertThat(diagnosticManager).isNotNull();
        assertThat(baseUrl.toString()).endsWith(contextPath + "/");
    }
}
