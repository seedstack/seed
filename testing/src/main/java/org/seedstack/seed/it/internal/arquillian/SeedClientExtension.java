/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it.internal.arquillian;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;

/**
 * Arquillian extension to register SEED support in the client.
 *
 * @author adrien.lauer@mpsa.com
 */
public class SeedClientExtension implements LoadableExtension {
    @Override
    public void register(final ExtensionBuilder builder) {
        // append the SEED arquillian support to the deployed app
        builder.service(AuxiliaryArchiveAppender.class, SeedArchiveAppender.class);

        // registers the enricher for embedded containers
        builder.service(TestEnricher.class, InjectionEnricher.class);
    }
}