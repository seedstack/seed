/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell;

import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.it.AbstractSeedWebIT;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.URL;
import java.security.PublicKey;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class ShellInWebappIT extends AbstractSeedWebIT {
    @Inject
    private Application application;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsResource("META-INF/configuration/org.seedstack.seed.shell.props")
                .setWebXML("WEB-INF/web.xml");
    }

    @Test
    @RunAsClient
    public void web_security_is_working(@ArquillianResource URL baseURL) throws Exception {
        expect().statusCode(401).when().get(baseURL.toString() + "resources/protected.html");
        given().auth().basic("test", "good").expect().statusCode(200).when().get(baseURL.toString() + "resources/protected.html");
    }

    @Test
    @RunAsClient
    public void shell_security_is_working() throws Exception {
        SSHClient sshClient = new SSHClient();
        try {
            sshClient.addHostKeyVerifier(new HostKeyVerifier() {
                @Override
                public boolean verify(String hostname, int port, PublicKey key) {
                    return true;
                }
            });
            sshClient.connect(InetAddress.getLocalHost(), application.getConfiguration().getInt(("org.seedstack.seed.shell.port")));
            sshClient.authPassword("test", "good");
            assertThat(sshClient.isAuthenticated()).isTrue();
        } finally {
            sshClient.close();
        }
    }
}
