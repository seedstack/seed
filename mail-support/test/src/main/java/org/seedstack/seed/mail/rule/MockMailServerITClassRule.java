/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.mail.rule;

import org.seedstack.seed.it.api.ITBind;
import org.seedstack.seed.mail.api.WithMailServer;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This junit Rule is responsible of configuring a (@Link Wiser) instance with parameters
 * provided through (@link WithMailServer) annotation and starting the instance before
 * method test and stopping the instance after test execution
 *
 *
 */
@ITBind
public class MockMailServerITClassRule extends ExternalResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockMailServerITClassRule.class);

    @Inject
    private Wiser mailServer;

    @Override
    protected void before() throws Throwable {
        if (!mailServer.getServer().isRunning()) {
            LOGGER.info("Starting mail server {} ", mailServer.hashCode());
            mailServer.start();
        }
    }

    @Override
    protected void after() {
        if (mailServer.getServer().isRunning()) {
            LOGGER.info("Stopping mail server {} ", mailServer.hashCode());
            mailServer.stop();
        }
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        final Class<?> testClass = description.getTestClass();
        if (testClass != null) {
            final WithMailServer withMailServer = testClass.getAnnotation(WithMailServer.class);
            if (withMailServer != null) {
                configureServer(withMailServer);
            }
        }
        return super.apply(base, description);
    }

    private void configureServer(WithMailServer withMailServer) {
        final int port = withMailServer.port();
        final String host = withMailServer.host();

        configureServer(port, host);
    }

    private void configureServer(int port, String host) {
        mailServer.setPort(port);
        try {
            mailServer.getServer().setBindAddress(InetAddress.getByName(host));
        } catch (UnknownHostException e) {
            //if not a valid binding address then fall back to host name as listen address
            LOGGER.info("Invalid binding address, using it as hostname instead", e);
            mailServer.setHostname(host);
        }
    }
}
