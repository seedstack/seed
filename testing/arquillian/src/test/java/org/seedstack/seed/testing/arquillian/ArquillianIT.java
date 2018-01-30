/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.testing.arquillian;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Application;

@RunWith(Arquillian.class)
public class ArquillianIT {
    @Inject
    private Application application;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Test
    public void testServerMethod() {
        //assertThat(application).isNotNull(); FIXME: arquillian tests are not injected server-side
    }

    @Test
    @RunAsClient
    public void testClientMethod(@ArquillianResource URL url) {
        assertThat(url).isNotNull();
        assertThat(application).isNotNull();
    }
}
