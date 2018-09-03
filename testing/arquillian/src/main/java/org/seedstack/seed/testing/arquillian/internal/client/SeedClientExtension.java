/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.testing.arquillian.internal.client;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.seedstack.seed.testing.arquillian.internal.InjectionTestEnricher;

/**
 * Arquillian extension to register SeedStack support in the client.
 */
public class SeedClientExtension implements LoadableExtension {
    @Override
    public void register(final ExtensionBuilder builder) {
        // append arquillian support to the deployed app
        builder.service(AuxiliaryArchiveAppender.class, SeedArchiveAppender.class);
        // register the injection enricher
        builder.service(TestEnricher.class, InjectionTestEnricher.class);
    }
}